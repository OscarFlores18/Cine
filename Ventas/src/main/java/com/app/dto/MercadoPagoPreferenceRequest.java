package com.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Body de POST /checkout/preferences de la API real de Mercado Pago.
 * Los nombres van en snake_case (@JsonProperty) porque asi los espera la API.
 */
public record MercadoPagoPreferenceRequest(
    List<Item> items,
    @JsonProperty("external_reference") String externalReference,
    @JsonProperty("notification_url") String notificationUrl
) {
    public record Item(
        String title,
        Integer quantity,
        @JsonProperty("unit_price") BigDecimal unitPrice
    ) {}
}
