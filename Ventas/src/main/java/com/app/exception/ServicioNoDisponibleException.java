package com.app.exception;

/**
 * Un servicio del que Ventas depende (MS Sala, MS Productos o Mercado Pago)
 * no respondio (caido, timeout, 5xx). GlobalExceptionHandler -> 503.
 */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String message) {
        super(message);
    }
}
