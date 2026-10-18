package com.app.service;

import com.app.client.AuthClient;
import com.app.client.MercadoPagoClient;
import com.app.client.ProductoClient;
import com.app.client.SalaClient;
import com.app.config.RabbitMQConfig;
import com.app.dto.ButacaDTO;
import com.app.dto.FuncionDTO;
import com.app.dto.MercadoPagoPaymentResponse;
import com.app.dto.MercadoPagoPreferenceRequest;
import com.app.dto.MercadoPagoPreferenceResponse;
import com.app.dto.MercadoPagoRefundResponse;
import com.app.dto.MercadoPagoWebhookDTO;
import com.app.dto.ProductoCompraDTO;
import com.app.dto.ProductoDTO;
import com.app.dto.SalaDTO;
import com.app.dto.UsuarioDTO;
import com.app.dto.VentaAltaDTO;
import com.app.dto.VentaDTO;
import com.app.enums.EstadoReembolso;
import com.app.enums.EstadoVenta;
import com.app.exception.ButacaNoDisponibleException;
import com.app.exception.PagoRechazadoException;
import com.app.exception.RecursoNoEncontradoException;
import com.app.exception.ServicioNoDisponibleException;
import com.app.exception.SolicitudInvalidaException;
import com.app.mapper.IMapper;
import com.app.model.Venta;
import com.app.model.VentaButaca;
import com.app.model.VentaProducto;
import com.app.repository.VentaRepository;
import com.app.senders.EventoEntradaCompradaDTO;
import com.app.senders.FacturaItemDTO;
import com.app.senders.VentaCanceladaEvent;
import com.app.senders.VentaConfirmadaEvent;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    // Valores del enum EstadoButaca real de MS Sala.
    private static final String BUTACA_DISPONIBLE = "DISPONIBLE";
    private static final String BUTACA_OCUPADO = "OCUPADO";

    private final VentaRepository ventaRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SalaClient salaClient;
    private final ProductoClient productoClient;
    private final AuthClient authClient;
    private final MercadoPagoClient mercadoPagoClient;

    @Value("${mercadopago.notification-url:http://localhost:8081/ventas/webhook/mercadopago}")
    private String notificationUrl;

    @Override
    @Transactional
    public VentaDTO altaVenta(IMapper<Venta> mapper, String username) {
        Venta venta = mapper.mapperTo();
        venta.setUsername(username);
        venta.setFecha(LocalDateTime.now());

        VentaAltaDTO dto = dto(mapper);
        List<Long> butacaIds = dto != null ? dto.butacaIds() : List.of();
        if (butacaIds == null || butacaIds.isEmpty()) {
            throw new SolicitudInvalidaException("Hay que elegir al menos una butaca");
        }

        // --- 1) Revalidar TODO en el servidor: nunca confiar en lo que el
        //     cliente vio al navegar, puede haber cambiado. ---

        FuncionDTO funcion = obtenerFuncion(venta.getFuncionId());
        List<ButacaDTO> butacasSala = obtenerButacasDeSala(funcion.salaId());
        Map<Long, ButacaDTO> porId = butacasSala.stream()
                .collect(Collectors.toMap(ButacaDTO::id, b -> b));

        BigDecimal precioEntrada = funcion.precioBase() != null ? funcion.precioBase() : BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (Long butacaId : butacaIds) {
            ButacaDTO butaca = porId.get(butacaId);
            if (butaca == null) {
                throw new SolicitudInvalidaException(
                        "La butaca " + butacaId + " no existe en la sala de la funcion " + funcion.id());
            }
            // NOTA / limitacion conocida: entre este chequeo y el momento en
            // que la butaca se marca OCUPADO (recien al confirmarse el pago,
            // no aca) hay una ventana en la que dos personas podrian ver la
            // misma butaca "disponible" y generar dos preferencias de pago
            // para ella. Una version mas robusta la reservaria con un
            // estado intermedio (RESERVADA, con expiracion) al generar la
            // preferencia. Queda fuera del alcance de esta base.
            if (!butaca.disponible()) {
                throw new ButacaNoDisponibleException("La butaca " + butaca.fila() + butaca.numero() + " no esta disponible");
            }
            venta.addButaca(new VentaButaca(butaca.id(), butaca.fila(), butaca.numero()));
            total = total.add(precioEntrada);
        }

        if (dto != null && dto.productos() != null) {
            for (ProductoCompraDTO linea : dto.productos()) {
                ProductoDTO producto = obtenerProducto(linea.productoId());

                if (producto.stock() == null || producto.stock() < linea.cantidad()) {
                    throw new ButacaNoDisponibleException("No hay stock suficiente de " + producto.nombre());
                }

                VentaProducto vp = new VentaProducto();
                vp.setProductoId(producto.id());
                vp.setCantidad(linea.cantidad());
                vp.setPrecioUnitario(producto.precio());
                venta.addProducto(vp);

                total = total.add(vp.subtotal());
            }
        }

        venta.setTotal(total);
        venta.setEstado(EstadoVenta.PENDIENTE);

        // Hace falta el id para armar external_reference, por eso se guarda
        // (flush) ANTES de pedirle la preferencia a Mercado Pago.
        venta = ventaRepository.saveAndFlush(venta);

        // Resiliencia de demo: si Mercado Pago rechaza o no responde (cuenta
        // sin verificar, token de prueba con restricciones, servicio caido,
        // etc.) la venta queda creada igual en PENDIENTE, sin link de pago
        // real. Antes esto deshacia toda la transaccion y no quedaba nada
        // creado — un problema de cuenta/credenciales de Mercado Pago no
        // deberia tumbar la compra entera.
        String initPoint = null;
        try {
            MercadoPagoPreferenceResponse preferencia = crearPreferenciaPago(venta, precioEntrada);
            venta.setPreferenceId(preferencia.id());
            initPoint = preferencia.initPoint();
        } catch (PagoRechazadoException | ServicioNoDisponibleException e) {
            log.warn("No se pudo generar la preferencia de pago en Mercado Pago para la venta {}: {}. " +
                    "La venta queda creada en PENDIENTE sin link de pago real.", venta.getId(), e.getMessage());
        }

        return VentaDTO.from(venta, initPoint);
    }

    @Override
    public List<VentaDTO> listarVentas() {
        return ventaRepository.findAll().stream().map(VentaDTO::from).toList();
    }

    @Override
    public List<VentaDTO> listarVentasDeUsuario(String username) {
        return ventaRepository.findByUsername(username).stream().map(VentaDTO::from).toList();
    }

    @Override
    public VentaDTO obtenerPorId(Long id) {
        return VentaDTO.from(buscar(id));
    }

    @Override
    @Transactional
    public VentaDTO cancelarVenta(Long id) {
        Venta venta = buscar(id);

        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new ButacaNoDisponibleException("La venta " + id + " ya estaba cancelada");
        }

        EstadoVenta estadoAnterior = venta.getEstado();
        venta.setEstado(EstadoVenta.CANCELADA);

        // Las butacas se liberan SIEMPRE, independientemente de lo que pase
        // con el reembolso (ver mas abajo). Publicar es "nice to have": si
        // RabbitMQ/Sala estan caidos, se loguea y se sigue (ver publicar()).
        for (VentaButaca vb : venta.getButacas()) {
            publicarCambioButaca(vb.getButacaId(), BUTACA_DISPONIBLE);
        }

        // El reembolso es un "alt" aparte (igual que en el diagrama de
        // secuencia): si la venta ya estaba PAGADA se intenta reembolsar en
        // Mercado Pago; si no, no hay nada que reembolsar. Si el reembolso
        // falla, NO revierte la cancelacion: queda FALLIDO para resolverlo
        // a mano despues.
        if (estadoAnterior == EstadoVenta.PAGADA) {
            venta.setEstadoReembolso(intentarReembolso(venta));
        } else {
            venta.setEstadoReembolso(EstadoReembolso.NO_APLICA);
        }

        venta = ventaRepository.save(venta);

        publicar(RabbitMQConfig.CANCELADA_ROUTING_KEY, new VentaCanceladaEvent(venta.getId(), venta.getUsername()));

        return VentaDTO.from(venta);
    }

    @Override
    @Transactional
    public void procesarWebhookMercadoPago(MercadoPagoWebhookDTO webhook) {
        if (webhook == null || webhook.data() == null || webhook.data().id() == null
                || !"payment".equalsIgnoreCase(webhook.type())) {
            // Mercado Pago manda otros tipos de notificacion (merchant_order,
            // etc.) que no nos interesan: se ignoran sin romper nada.
            return;
        }

        MercadoPagoPaymentResponse pago;
        try {
            pago = mercadoPagoClient.obtenerPago(webhook.data().id());
        } catch (FeignException e) {
            log.error("No se pudo consultar el pago {} en Mercado Pago: {}", webhook.data().id(), e.getMessage());
            return; // Mercado Pago reintenta el webhook mas tarde si hace falta
        }

        if (!pago.aprobado()) {
            log.info("Webhook de pago {} con estado {}: no se confirma la venta todavia", pago.id(), pago.status());
            return;
        }

        Long ventaId;
        try {
            ventaId = Long.valueOf(pago.externalReference());
        } catch (NumberFormatException e) {
            log.error("external_reference invalido en el pago {}: {}", pago.id(), pago.externalReference());
            return;
        }

        Venta venta = ventaRepository.findById(ventaId).orElse(null);
        if (venta == null) {
            log.error("Webhook de pago {} referencia la venta {}, que no existe", pago.id(), ventaId);
            return;
        }

        // Idempotencia: Mercado Pago puede reenviar el mismo webhook.
        if (venta.getEstado() == EstadoVenta.PAGADA) {
            return;
        }
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            log.warn("Llego confirmacion de pago para la venta {} pero ya estaba CANCELADA", ventaId);
            return;
        }

        venta.setEstado(EstadoVenta.PAGADA);
        venta.setPaymentId(String.valueOf(pago.id()));

        // El pago ya esta acreditado: las butacas se ocupan aunque Sala este
        // caida en este instante (se loguea para seguimiento manual).
        for (VentaButaca vb : venta.getButacas()) {
            publicarCambioButaca(vb.getButacaId(), BUTACA_OCUPADO);
        }

        ventaRepository.save(venta);

        publicar(RabbitMQConfig.CONFIRMADA_ROUTING_KEY, armarFactura(venta));
    }

    // Arma el evento "factura" que recibe MS Email. La venta ya esta pagada
    // y confirmada en este punto: nada de lo que pase aca puede hacer
    // fallar la confirmacion, asi que cada dato "extra" (sala, nombre real)
    // se pide por separado y se degrada a null/placeholder si falla, en vez
    // de propagar la excepcion.
    private VentaConfirmadaEvent armarFactura(Venta venta) {
        VentaConfirmadaEvent evento = new VentaConfirmadaEvent();
        evento.setIdVenta(venta.getId());
        evento.setUsername(venta.getUsername());
        evento.setFuncionId(venta.getFuncionId());
        evento.setPeliculaTitulo(venta.getPeliculaTitulo());
        evento.setNumeroCompra("V-" + venta.getId());
        evento.setFecha(venta.getFecha());
        evento.setTotal(venta.getTotal());
        evento.setButacas(venta.getButacas().stream()
                .map(vb -> (vb.getFila() != null ? vb.getFila() : "") + (vb.getNumero() != null ? vb.getNumero() : ""))
                .toList());

        UsuarioDTO usuario = obtenerUsuarioOpcional(venta.getUsername());
        if (usuario != null) {
            evento.setNombreCompleto(usuario.nombreCompleto());
        }

        FuncionDTO funcion = obtenerFuncionOpcional(venta.getFuncionId());
        BigDecimal precioEntrada = BigDecimal.ZERO;
        if (funcion != null) {
            evento.setFuncionFechaHora(parseFechaHora(funcion.fechaHora()));
            precioEntrada = funcion.precioBase() != null ? funcion.precioBase() : BigDecimal.ZERO;

            SalaDTO sala = obtenerSalaOpcional(funcion.salaId());
            if (sala != null) {
                evento.setSalaNombre(sala.nombre());
                evento.setSalaTipo(sala.tipo());
            } else if (funcion.nombreSala() != null) {
                evento.setSalaNombre(funcion.nombreSala());
            }
        }

        int cantButacas = venta.getButacas().size();
        BigDecimal subtotalEntradas = precioEntrada.multiply(BigDecimal.valueOf(cantButacas));
        String etiquetaButacas = evento.getButacas().isEmpty() ? "Entrada" : String.join(", ", evento.getButacas());
        evento.setEntradas(List.of(new FacturaItemDTO(
                "Entrada (butacas " + etiquetaButacas + ")", cantButacas, precioEntrada, subtotalEntradas)));
        evento.setSubtotalEntradas(subtotalEntradas);

        List<FacturaItemDTO> items = new ArrayList<>();
        BigDecimal subtotalProductos = BigDecimal.ZERO;
        for (VentaProducto vp : venta.getProductos()) {
            String nombre = obtenerNombreProductoOpcional(vp.getProductoId());
            items.add(new FacturaItemDTO(nombre, vp.getCantidad(), vp.getPrecioUnitario(), vp.subtotal()));
            subtotalProductos = subtotalProductos.add(vp.subtotal());
        }
        evento.setProductos(items);
        evento.setSubtotalProductos(subtotalProductos);

        return evento;
    }

    private LocalDateTime parseFechaHora(String fechaHora) {
        if (fechaHora == null) return null;
        try {
            return LocalDateTime.parse(fechaHora);
        } catch (Exception e) {
            return null;
        }
    }

    // Variantes "opcionales" que no tiran excepcion: se usan solo para
    // enriquecer la factura del mail, despues de que la venta ya quedo
    // confirmada y pagada.
    private FuncionDTO obtenerFuncionOpcional(Long funcionId) {
        try {
            return salaClient.obtenerFuncion(funcionId);
        } catch (FeignException e) {
            log.warn("No se pudo completar la funcion {} para la factura (no bloquea la venta): {}", funcionId, e.getMessage());
            return null;
        }
    }

    private SalaDTO obtenerSalaOpcional(Long salaId) {
        if (salaId == null) return null;
        try {
            return salaClient.obtenerSala(salaId);
        } catch (FeignException e) {
            log.warn("No se pudo completar la sala {} para la factura (no bloquea la venta): {}", salaId, e.getMessage());
            return null;
        }
    }

    private UsuarioDTO obtenerUsuarioOpcional(String username) {
        try {
            return authClient.obtenerUsuario(username);
        } catch (FeignException e) {
            log.warn("No se pudo resolver nombre/apellido de {} para la factura (no bloquea la venta): {}", username, e.getMessage());
            return null;
        }
    }

    private String obtenerNombreProductoOpcional(Long productoId) {
        try {
            return productoClient.obtenerProducto(productoId).nombre();
        } catch (FeignException e) {
            log.warn("No se pudo completar el nombre del producto {} para la factura (no bloquea la venta): {}", productoId, e.getMessage());
            return "Producto #" + productoId;
        }
    }

    // ---------------------------------------------------------------------

    private Venta buscar(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la venta con id " + id));
    }

    private FuncionDTO obtenerFuncion(Long funcionId) {
        try {
            return salaClient.obtenerFuncion(funcionId);
        } catch (FeignException.NotFound e) {
            throw new SolicitudInvalidaException("La funcion " + funcionId + " no existe");
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException(
                    "No se pudo verificar la funcion " + funcionId + " (el microservicio Sala no responde). Intenta de nuevo.");
        }
    }

    // Unica forma real de leer butacas: la lista completa de la sala (no
    // existe un GET por id singular en el Sala real). Ventas filtra sobre
    // esa lista las butacaIds pedidas.
    private List<ButacaDTO> obtenerButacasDeSala(Long salaId) {
        try {
            return salaClient.obtenerButacasPorSala(salaId);
        } catch (FeignException.NotFound e) {
            throw new SolicitudInvalidaException("La sala " + salaId + " no existe");
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException(
                    "No se pudo verificar las butacas de la sala " + salaId + " (el microservicio Sala no responde). Intenta de nuevo.");
        }
    }

    private ProductoDTO obtenerProducto(Long productoId) {
        try {
            return productoClient.obtenerProducto(productoId);
        } catch (FeignException.NotFound e) {
            throw new SolicitudInvalidaException("El producto " + productoId + " no existe");
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException(
                    "No se pudo verificar el producto " + productoId + " (el microservicio Productos no responde). Intenta de nuevo.");
        }
    }

    private MercadoPagoPreferenceResponse crearPreferenciaPago(Venta venta, BigDecimal precioEntrada) {
        List<MercadoPagoPreferenceRequest.Item> items = new ArrayList<>();
        for (VentaButaca vb : venta.getButacas()) {
            items.add(new MercadoPagoPreferenceRequest.Item(
                    "Entrada - butaca " + vb.getFila() + vb.getNumero() + " (funcion " + venta.getFuncionId() + ")",
                    1, precioEntrada));
        }
        for (VentaProducto vp : venta.getProductos()) {
            items.add(new MercadoPagoPreferenceRequest.Item(
                    "Producto #" + vp.getProductoId(), vp.getCantidad(), vp.getPrecioUnitario()));
        }

        MercadoPagoPreferenceRequest request = new MercadoPagoPreferenceRequest(
                items, String.valueOf(venta.getId()), notificationUrl);

        try {
            return mercadoPagoClient.crearPreferencia(request);
        } catch (FeignException.FeignClientException e) {
            // 4xx de la API de Mercado Pago: rechazo a nivel de negocio
            // (datos invalidos, cuenta/credenciales rechazadas, etc.), no
            // una caida del servicio.
            throw new PagoRechazadoException("Mercado Pago rechazo la solicitud de pago: " + e.getMessage());
        } catch (FeignException e) {
            throw new ServicioNoDisponibleException(
                    "No se pudo generar la preferencia de pago en Mercado Pago. Intenta de nuevo.");
        }
    }

    // Si la venta estaba PAGADA, intenta reembolsar. Nunca tira: si falla
    // devuelve FALLIDO en vez de interrumpir la cancelacion.
    private EstadoReembolso intentarReembolso(Venta venta) {
        if (venta.getPaymentId() == null) {
            log.warn("La venta {} estaba PAGADA pero no tiene paymentId: no se puede reembolsar", venta.getId());
            return EstadoReembolso.FALLIDO;
        }
        try {
            MercadoPagoRefundResponse respuesta = mercadoPagoClient.reembolsar(venta.getPaymentId());
            return "approved".equalsIgnoreCase(respuesta.status()) ? EstadoReembolso.COMPLETADO : EstadoReembolso.FALLIDO;
        } catch (FeignException e) {
            log.error("No se pudo reembolsar el pago {} de la venta {} en Mercado Pago: {}",
                    venta.getPaymentId(), venta.getId(), e.getMessage());
            return EstadoReembolso.FALLIDO;
        }
    }

    // Publica el cambio de estado de UNA butaca hacia MS Sala. Async (no
    // bloquea la venta si Sala/RabbitMQ estan caidos) y reemplaza al viejo
    // "salaClient.cambiarEstadoButaca(...)" (que llamaba a un endpoint que
    // no existe en el Sala real) — ver RabbitMQConfig y VentasConsumer de Sala.
    private void publicarCambioButaca(Long butacaId, String nuevoEstado) {
        publicar(RabbitMQConfig.ROUTING_KEY_BUTACAS, RabbitMQConfig.EXCHANGE_VENTAS_SALA,
                new EventoEntradaCompradaDTO(butacaId, nuevoEstado));
    }

    // El cambio en la base ya se hizo. Publicar el evento es "nice to have":
    // si RabbitMQ no esta levantado, la operacion igual tiene que poder
    // completarse. Se loguea y se sigue, en vez de romper toda la request.
    //
    // El mensaje se manda DESPUES del commit: si la transaccion fallara y se
    // deshiciera, no tiene que haber salido un aviso de algo que no ocurrio.
    private void publicar(String routingKey, Object evento) {
        publicar(routingKey, RabbitMQConfig.EXCHANGE, evento);
    }

    private void publicar(String routingKey, String exchange, Object evento) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(exchange, routingKey, evento);
                }
            });
        } else {
            enviar(exchange, routingKey, evento);
        }
    }

    private void enviar(String exchange, String routingKey, Object evento) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, evento);
        } catch (AmqpException e) {
            log.warn("No se pudo publicar el evento {} ({} / {}) en RabbitMQ (¿esta levantado el broker?). " +
                    "El cambio igual se guardo. Detalle: {}", evento, exchange, routingKey, e.getMessage());
        }
    }

    // Pequeno helper: el controller recibe IMapper<Venta> (igual que el
    // patron de Alumno en la base de escuela), pero aca hace falta tambien
    // la lista de butacas/productos que trae el DTO concreto (VentaAltaDTO),
    // que IMapper no expone. En vez de agregar eso a la interfaz generica
    // (rompería el contrato para otros usos futuros), el service hace un
    // chequeo de tipo acotado a este unico caso.
    private VentaAltaDTO dto(IMapper<Venta> mapper) {
        return mapper instanceof VentaAltaDTO v ? v : null;
    }
}
