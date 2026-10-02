package com.app.dto;

import com.app.enums.TipoSala;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SalaAltaDTO(
    @NotBlank String nombre,
    @NotNull @Positive Integer capacidad,
    @NotNull TipoSala tipo
) {}
