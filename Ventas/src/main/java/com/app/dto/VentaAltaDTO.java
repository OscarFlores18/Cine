package com.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

// OJO: por ahora el total lo manda el cliente. Mas adelante conviene que Ventas
// le pregunte el precio a Productos (Feign) y lo calcule ella, para no confiar
// en el valor que llega de afuera.
public record VentaAltaDTO(
    @NotNull Long productoId,
    @NotNull @Positive Integer cantidad,
    @NotNull @Positive BigDecimal total
) {}
