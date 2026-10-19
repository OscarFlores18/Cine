package com.app.dto;

import com.app.enums.EstadoReembolso;
import com.app.enums.EstadoVenta;
import com.app.model.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaDTO(
    Long id,
    String username,
    Long funcionId,
    String peliculaTitulo,
    List<VentaButacaDTO> butacas,
    EstadoVenta estado,
    EstadoReembolso estadoReembolso,
    BigDecimal total,
    LocalDateTime fecha,
    List<VentaProductoDTO> productos,
    // Solo viene completo en la respuesta del alta (o de reintentar-pago):
    // es la URL de Mercado Pago a la que el front tiene que mandar al
    // cliente a pagar. En cualquier otra respuesta (el listado de "Mis
    // compras", por ejemplo) viaja null -no se vuelve a pedir ni se
    // reconstruye, asi que NO sirve para saber si la venta "tiene o no
    // tiene" un link de pago (ver tienePreferenciaDePago mas abajo: antes
    // el front usaba initPoint==null para eso y terminaba mostrando
    // "Mercado Pago no respondió" en compras que en realidad ya tenian un
    // link activo y solo estaban esperando que el comprador la pagara, o
    // esperando que llegue el webhook de confirmacion).
    String initPoint,
    // true si esta venta llego a generar una preferencia de pago real en
    // Mercado Pago en algun momento (altaVenta o reintentar-pago), haya
    // sido pagada ya o no. El front la usa para decidir si ofrece
    // "Reintentar pago" (false: Mercado Pago nunca respondio) o si solo
    // hay que esperar la confirmacion del webhook (true + PENDIENTE).
    boolean tienePreferenciaDePago
) {
    public static VentaDTO from(Venta venta) {
        return from(venta, null);
    }

    public static VentaDTO from(Venta venta, String initPoint) {
        return new VentaDTO(
                venta.getId(),
                venta.getUsername(),
                venta.getFuncionId(),
                venta.getPeliculaTitulo(),
                venta.getButacas().stream().map(VentaButacaDTO::from).toList(),
                venta.getEstado(),
                venta.getEstadoReembolso(),
                venta.getTotal(),
                venta.getFecha(),
                venta.getProductos().stream().map(VentaProductoDTO::from).toList(),
                initPoint,
                venta.getPreferenceId() != null
        );
    }
}
