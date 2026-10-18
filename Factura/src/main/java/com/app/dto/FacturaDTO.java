package com.app.dto;

import com.app.model.Factura;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FacturaDTO(
    Long id,
    Long ventaId,
    String numeroCompra,
    String username,
    String nombreCompleto,
    Long funcionId,
    String peliculaTitulo,
    String salaNombre,
    String salaTipo,
    LocalDateTime funcionFechaHora,
    LocalDateTime fecha,
    List<String> butacas,
    List<FacturaItemEventDTO> entradas,
    List<FacturaItemEventDTO> productos,
    BigDecimal subtotalEntradas,
    BigDecimal subtotalProductos,
    BigDecimal total
) {
    public static FacturaDTO from(Factura f) {
        return new FacturaDTO(
                f.getId(), f.getVentaId(), f.getNumeroCompra(), f.getUsername(), f.getNombreCompleto(),
                f.getFuncionId(), f.getPeliculaTitulo(), f.getSalaNombre(), f.getSalaTipo(),
                f.getFuncionFechaHora(), f.getFecha(), f.getButacas(),
                f.getEntradas().stream().map(i -> new FacturaItemEventDTO(i.getNombre(), i.getCantidad(), i.getPrecioUnitario(), i.getSubtotal())).toList(),
                f.getProductos().stream().map(i -> new FacturaItemEventDTO(i.getNombre(), i.getCantidad(), i.getPrecioUnitario(), i.getSubtotal())).toList(),
                f.getSubtotalEntradas(), f.getSubtotalProductos(), f.getTotal()
        );
    }
}
