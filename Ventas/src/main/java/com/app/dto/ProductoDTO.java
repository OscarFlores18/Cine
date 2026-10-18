package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Los datos del producto de confiteria que Ventas necesita de MS Productos
 * (precio real y stock) para validar y tasar la compra. Coincide con el
 * Producto real que ya expone GET /productos/{id}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoDTO(
    Long id,
    String nombre,
    BigDecimal precio,
    Integer stock
) {}
