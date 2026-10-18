package com.app.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Linea de la factura (una entrada o un producto de confiteria), embebida. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturaItem {
    private String nombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
