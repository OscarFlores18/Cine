package com.app.exception;

/**
 * Conflicto de estado dentro de Ventas: la butaca ya esta ocupada, no hay
 * stock suficiente de algun producto, o la venta ya estaba cancelada.
 * Nombrada igual que en el diagrama del Figma. GlobalExceptionHandler -> 409.
 */
public class ButacaNoDisponibleException extends RuntimeException {

    public ButacaNoDisponibleException(String message) {
        super(message);
    }
}
