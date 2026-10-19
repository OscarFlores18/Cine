package com.app.senders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Evento que Ventas publica en RabbitMQ cuando se cancela una venta (propia
 * o por un admin/empleado). MS Email lo escucha para mandar el mail de
 * cancelacion -antes este evento solo traia idVenta/username y nada lo
 * consumia del lado de Email, asi que nunca salia ningun correo.
 *
 * Mismo criterio que VentaConfirmadaEvent: es un contrato propio, separado
 * de la entidad Venta, y el consumer (Email) tiene su propia copia de esta
 * clase con los mismos nombres de campo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaCanceladaEvent {

    private Long idVenta;
    private String username;

    // Nombre y apellido reales (resueltos contra Auth), puede venir null si
    // Auth no respondio -no bloquea la cancelacion.
    private String nombreCompleto;

    private String numeroCompra;
    private String peliculaTitulo;
    private List<String> butacas;
    private BigDecimal total;

    // "COMPLETADO", "FALLIDO" o "NO_APLICA" (ver EstadoReembolso) -asi el
    // mail le puede avisar al cliente si el reembolso salio bien, si quedo
    // pendiente de resolver a mano, o si directamente no correspondia
    // (la venta nunca llego a pagarse).
    private String estadoReembolso;
}
