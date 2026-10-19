package com.app.dto;

/**
 * Espejo del ProductoStockDTO que expone MS Productos en
 * PATCH /productos/{id}/stock: ajuste relativo de stock, positivo repone,
 * negativo descuenta.
 */
public record AjustarStockDTO(Integer cantidad) {}
