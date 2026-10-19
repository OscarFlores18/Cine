package com.app.Gateway.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Misma forma de error que usa el resto del sistema (ver Ventas.ApiError).
 * El Gateway es la unica pieza que puede llegar a cortar una request ANTES
 * de que llegue a ningun microservicio de dominio (JWT ausente/invalido:
 * 401, rol sin permiso: 403) -antes esos casos devolvian el 401/403 "pelado"
 * de Spring Security (sin body, o un body que no es JSON), asi que el front
 * no podia mostrar nada util y mucho menos distinguirlo de una sesion
 * vencida real (ver auth.interceptor.ts).
 *
 * Es un record (y no una clase @Data como en los otros modulos) porque este
 * modulo no tiene Lombok como dependencia y no vale la pena agregarlo solo
 * para esto.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
