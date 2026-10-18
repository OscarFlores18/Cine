package com.app.senders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Evento que Ventas publica en RabbitMQ cuando un admin cancela una venta. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaCanceladaEvent {

    private Long idVenta;
    private String username;
}
