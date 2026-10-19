package com.app.exception;

/**
 * El usuario logueado (segun X-User-Username/X-User-Roles) intento operar
 * sobre una venta que no es suya, sin tener rol ADMIN/EMPLEADO para hacerlo
 * de todas formas (ej: cancelar o reintentar el pago de la compra de otro
 * usuario). GlobalExceptionHandler -> 403.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String message) {
        super(message);
    }
}
