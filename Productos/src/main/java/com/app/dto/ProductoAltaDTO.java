package com.app.dto;

import com.app.enums.CategoriaProducto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductoAltaDTO(
    @NotBlank String nombre,
    String descripcion,
    @NotNull @Positive BigDecimal precio,
    @NotNull @PositiveOrZero Integer stock,
    @NotNull CategoriaProducto categoria
) {}
