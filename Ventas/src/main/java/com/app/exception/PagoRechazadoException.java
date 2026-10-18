package com.app.exception;

/**
 * Mercado Pago rechazo la solicitud a nivel de negocio (4xx de su API: datos
 * invalidos, credenciales/cuenta rechazada, etc.) al crear la preferencia de
 * pago. Distinto de que Mercado Pago este caido (ver
 * ServicioNoDisponibleException). GlobalExceptionHandler -> 402.
 */
public class PagoRechazadoException extends RuntimeException {

    public PagoRechazadoException(String message) {
        super(message);
    }
}
