package com.app.enums;

/**
 * Ciclo de vida de una Venta.
 *
 * PENDIENTE  -> se creo la venta y se genero la preferencia de pago en
 *               Mercado Pago, pero el cliente todavia no pago.
 * PAGADA     -> Mercado Pago confirmo el pago (webhook). La butaca ya quedo
 *               OCUPADA en MS Sala.
 * CANCELADA  -> el administrador la cancelo (ver {@code EstadoReembolso}
 *               para saber que paso con el dinero, si lo habia).
 */
public enum EstadoVenta {
    PENDIENTE,
    PAGADA,
    CANCELADA
}
