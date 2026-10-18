package com.app.exception;

/**
 * Datos de entrada invalidos a nivel de negocio: por ejemplo la funcion o el
 * producto referenciado no existe en MS Sala / MS Productos (Feign
 * devolvio 404). GlobalExceptionHandler -> 400.
 */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String message) {
        super(message);
    }
}
