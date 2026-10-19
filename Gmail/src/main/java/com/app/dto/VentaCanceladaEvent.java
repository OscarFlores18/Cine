package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Copia EXACTA (lado CONSUMIDOR) del contrato que publica MS Ventas:
 * com.app.senders.VentaCanceladaEvent. Mismos nombres de campo porque viaja
 * como JSON — si Ventas cambia esta clase, esta copia tiene que
 * actualizarse igual.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VentaCanceladaEvent(
    Long idVenta,
    String username,
    String nombreCompleto,
    String numeroCompra,
    String peliculaTitulo,
    List<String> butacas,
    BigDecimal total,
    String estadoReembolso
) {}
