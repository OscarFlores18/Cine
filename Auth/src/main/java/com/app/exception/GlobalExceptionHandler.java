package com.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handler de errores de Auth. Antes este modulo no tenia NINGUN manejo de
 * excepciones propio: un @Valid que fallaba, un body mal formado o
 * cualquier excepcion no prevista caian en la pagina de error generica de
 * Spring (Whitelabel / JSON por defecto, sin el formato {@link ApiError}
 * que usa el resto del sistema). Los errores de negocio puntuales
 * (login/refresh/registro invalidos, etc.) se siguen armando a mano en
 * AuthController -ahi SI se usa ApiError, pero via ResponseEntity directo,
 * no con una excepcion propia- asi que este handler cubre el resto:
 * validacion, body ilegible y cualquier excepcion no prevista.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - errores de validacion del body (@Valid): un detalle por campo.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Datos invalidos", req, detalles);
    }

    // 400 - un query param o path trae un valor que no se puede convertir.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Valor invalido para '" + ex.getName() + "': " + ex.getValue(), req, null);
    }

    // 400 - body mal formado (JSON invalido) o con tipos incorrectos.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleBodyIlegible(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "La solicitud tiene datos mal formados.", req, null);
    }

    // 500 - fallback: cualquier error no previsto se loguea y se devuelve un mensaje generico.
    // Importante en Auth en particular: nunca filtrar el mensaje crudo de la
    // excepcion aca (podria exponer detalles internos de autenticacion).
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleErrorInterno(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en Auth", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado", req, null);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req, List<String> details) {
        ApiError error = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(req.getRequestURI())
                .details(details)
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
