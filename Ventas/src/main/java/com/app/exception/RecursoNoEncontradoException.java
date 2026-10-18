package com.app.exception;

/** La venta pedida (por id) no existe. GlobalExceptionHandler -> 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
