package com.app.exception;

/** Se lanza al intentar operar (ej: ajustar stock) sobre un producto dado de baja. */
public class ProductoInactivoException extends RuntimeException {

    public ProductoInactivoException(String mensaje) {
        super(mensaje);
    }
}
