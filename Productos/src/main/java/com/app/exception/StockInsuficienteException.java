package com.app.exception;

/** Se lanza cuando un ajuste de stock dejaria el producto con stock negativo. */
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
