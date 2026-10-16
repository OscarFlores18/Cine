package com.app.exception;

/** Se lanza cuando ya existe un producto con el mismo nombre. */
public class ProductoDuplicadoException extends RuntimeException {

    public ProductoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
