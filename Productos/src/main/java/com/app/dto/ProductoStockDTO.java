package com.app.dto;

import jakarta.validation.constraints.NotNull;

// Ajuste relativo de stock: positivo repone, negativo descuenta.
// Ej: cantidad = 10 suma 10 unidades; cantidad = -3 resta 3.
public record ProductoStockDTO(
    @NotNull Integer cantidad
) {}
