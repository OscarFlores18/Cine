package com.app.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Edicion de una funcion YA creada: a proposito NO permite cambiar la sala
 * (salaId). Si la funcion ya tiene entradas vendidas, cambiar de sala
 * invalidaria las butacas referenciadas en esas ventas — eso no es
 * "editar", es cancelar y crear otra.
 */
public record FuncionEditarDTO(
    @NotNull(message = "El ID de la película es obligatorio") Long peliculaId,
    @NotNull(message = "La fecha y hora son obligatorias") @Future(message = "La función debe ser a futuro") LocalDateTime fechaHora,
    @NotNull(message = "El precio es obligatorio") @Positive(message = "El precio debe ser positivo") BigDecimal precioBase
) {}
