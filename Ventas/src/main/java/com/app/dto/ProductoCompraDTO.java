package com.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Una linea de confiteria dentro de VentaAltaDTO: que producto y cuantos. */
public record ProductoCompraDTO(
    @NotNull Long productoId,
    @NotNull @Positive Integer cantidad
) {}
