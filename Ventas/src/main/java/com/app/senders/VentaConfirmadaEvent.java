package com.app.senders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento que Ventas publica en RabbitMQ cuando se confirma el pago de una
 * venta (webhook de Mercado Pago). MS Email lo escucha para mandar el mail
 * de confirmacion tipo "factura / resumen de compra" (como el ticket de
 * Cineplanet de referencia), con todas las butacas de la compra y el
 * nombre/apellido real de quien compro.
 *
 * Es un "contrato" propio, separado de la entidad Venta: si manana cambia la
 * entidad, no se rompe el que escucha. El consumer (Email) tiene que tener
 * su propia copia de esta clase, con los mismos nombres de campo.
 *
 * Los campos que dependen de Sala/Auth (sala, nombreCompleto) pueden venir
 * en null: si esos microservicios no responden al momento de confirmar el
 * pago, la venta se confirma igual (ya esta cobrada) y el mail sale con lo
 * que haya, sin esos datos — ver VentaServiceImpl.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaConfirmadaEvent {

    private Long idVenta;
    private String username;

    // Nombre y apellido reales del comprador (resueltos contra Auth), para
    // mostrar "comprado por" en la factura. Null si Auth no respondio.
    private String nombreCompleto;

    private Long funcionId;

    // Numero de compra que se le muestra al cliente (no es el id tecnico).
    private String numeroCompra;

    private LocalDateTime fecha;

    // Texto informativo mandado por el front al comprar (Productos no tiene
    // entidad Pelicula, asi que no hay forma de validarlo ni de pedirlo).
    private String peliculaTitulo;

    // Puede venir null si MS Sala no respondio al confirmar el pago (no bloquea la venta).
    private String salaNombre;
    private String salaTipo;

    private LocalDateTime funcionFechaHora;

    // Todas las butacas de la compra (antes era una sola butacaFila/butacaNumero).
    private List<String> butacas; // ej. ["J6", "J7", "J8"]

    private List<FacturaItemDTO> entradas;
    private List<FacturaItemDTO> productos;

    private BigDecimal subtotalEntradas;
    private BigDecimal subtotalProductos;
    private BigDecimal total;
}
