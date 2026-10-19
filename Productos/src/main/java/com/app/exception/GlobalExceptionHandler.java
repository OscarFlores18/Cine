package com.app.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - el recurso pedido no existe
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", ex.getMessage()));
    }

    // 409 - reglas de negocio: nombre duplicado, stock insuficiente, producto inactivo
    @ExceptionHandler({ProductoDuplicadoException.class, StockInsuficienteException.class,
            ProductoInactivoException.class})
    public ResponseEntity<Map<String, String>> conflicto(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", ex.getMessage()));
    }

    // 409 - choque de locking optimista: otro ajuste de stock se guardo
    // primero sobre el mismo producto (ver Producto.version). El llamador
    // (Ventas) decide si reintenta.
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> conflictoDeVersion(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", "El producto fue modificado por otra operacion al mismo tiempo. Reintentar."));
    }

    // 400 - errores de validacion del body (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errores);
    }

    // 400 - errores de validacion de parametros/path
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> validacionParametros(ConstraintViolationException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getConstraintViolations()
                .forEach(v -> errores.put(v.getPropertyPath().toString(), v.getMessage()));
        return ResponseEntity.badRequest().body(errores);
    }

    // 400 - un query param o path trae un valor que no se puede convertir (ej: categoria inexistente)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("mensaje", "Valor invalido para '" + ex.getName() + "': " + ex.getValue()));
    }

    // 400 - body mal formado o con tipos incorrectos
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> bodyIlegible(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("mensaje", "El cuerpo de la peticion es invalido o esta mal formado"));
    }

    // 400 - falta un parametro requerido
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> parametroFaltante(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("mensaje", "Falta el parametro requerido: " + ex.getParameterName()));
    }

    // 500 - fallback: cualquier error no previsto se loguea y se devuelve un mensaje generico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> errorInterno(Exception ex) {
        log.error("Error no controlado en Productos", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Ocurrio un error inesperado"));
    }
}
