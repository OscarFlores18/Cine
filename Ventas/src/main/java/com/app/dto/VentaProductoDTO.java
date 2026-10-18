package com.app.dto;

import com.app.model.VentaProducto;

import java.math.BigDecimal;

public record VentaProductoDTO(
    Long productoId,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {
    public static VentaProductoDTO from(VentaProducto vp) {
        return new VentaProductoDTO(vp.getProductoId(), vp.getCantidad(), vp.getPrecioUnitario(), vp.subtotal());
    }
}
