package com.app.enums;

/**
 * Solo tiene sentido cuando la Venta se cancela.
 *
 * NO_APLICA  -> la venta no estaba PAGADA cuando se cancelo: no habia nada
 *               que reembolsar.
 * COMPLETADO -> Mercado Pago confirmo el reembolso.
 * FALLIDO    -> se intento reembolsar y Mercado Pago no respondio bien (o no
 *               respondio). La cancelacion NUNCA espera a esto: la venta se
 *               cancela igual y el reembolso queda marcado para resolver
 *               despues a mano.
 */
public enum EstadoReembolso {
    NO_APLICA,
    COMPLETADO,
    FALLIDO
}
