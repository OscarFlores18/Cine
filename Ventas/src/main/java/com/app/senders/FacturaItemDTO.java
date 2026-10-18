package com.app.senders;

import java.math.BigDecimal;

/**
 * Una linea de la factura que viaja dentro de VentaConfirmadaEvent (una
 * entrada, o un producto de confiteria). No es una entidad ni se persiste:
 * es solo la forma en la que Email recibe cada renglon para armar el mail
 * (ver captura de ejemplo tipo "ticket de Cineplanet" que uso el equipo
 * como referencia).
 */
public record FacturaItemDTO(
    String nombre,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal
) {}
