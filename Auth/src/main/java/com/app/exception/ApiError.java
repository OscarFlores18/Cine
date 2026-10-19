package com.app.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Forma unica de respuesta de error de Auth, igual a la de Ventas. Antes
 * Auth ni siquiera devolvia JSON en sus errores de negocio (login invalido,
 * usuario deshabilitado, rol invalido, etc.): devolvia el mensaje como
 * String plano en el body, asi que el front no podia parsear nada util de
 * ahi (ver core/utils/error.util.ts) y mostraba el mensaje generico de
 * fallback en vez del real.
 *
 * - timestamp: momento exacto del error.
 * - status:    codigo HTTP numerico (401, 404, etc.).
 * - error:     el "reason phrase" de ese status (ej. "Unauthorized").
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
