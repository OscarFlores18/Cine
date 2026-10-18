package com.app.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Forma unica de respuesta de error de Ventas: mas rica que el
 * {@code ErrorDTO{code,message}} que dibuja el Figma del equipo, para que el
 * front tenga toda la info util sin tener que pegarle a otro endpoint
 * (cuando paso, por que status, en que request).
 *
 * - timestamp: momento exacto del error.
 * - status:    codigo HTTP numerico (404, 409, etc.).
 * - error:     el "reason phrase" de ese status (ej. "Not Found").
 * - message:   el mensaje de negocio (el que ya tenian las excepciones).
 * - path:      el endpoint que se llamo.
 * - details:   opcional, lista de detalles (ej. un item por campo invalido
 *              en una validacion de @Valid). Se omite del JSON si queda null.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> details;
}
