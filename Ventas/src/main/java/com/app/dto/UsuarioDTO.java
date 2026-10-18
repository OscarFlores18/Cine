package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Nombre/apellido reales del comprador, resueltos contra Auth. Se usan para
 * que la factura/resumen de compra muestre quien compro (igual que el
 * ticket de Cineplanet que se tomo de referencia), en vez de solo el
 * username.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioDTO(String username, String nombre, String apellido) {

    public String nombreCompleto() {
        if (nombre == null && apellido == null) return null;
        return ((nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "")).trim();
    }
}
