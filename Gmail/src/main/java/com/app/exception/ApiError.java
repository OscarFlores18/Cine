package com.app.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Forma unica de respuesta de error de Gmail, igual a la de Ventas: antes
 * este modulo devolvia solo {@code {"mensaje": "..."}}, que el front no
 * sabia interpretar de la misma forma que a Ventas (ver
 * core/utils/error.util.ts) y terminaba mostrando mensajes incompletos o
 * genericos.
 *
 * - timestamp: momento exacto del error.
 * - status:    codigo HTTP numerico (404, 409, etc.).
 * - error:     el "reason phrase" de ese status (ej. "Not Found").
 * - message:   el mensaje de negocio.
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
