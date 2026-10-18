package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Notificacion que manda Mercado Pago a notification_url cuando cambia un
 * pago. Formato real (resumido a lo que usamos):
 *   { "type": "payment", "data": { "id": "1234567" } }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoWebhookDTO(
    String type,
    Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(String id) {}
}
