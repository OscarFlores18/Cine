package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Respuesta de GET /v1/payments/{id}: lo que el webhook usa para confirmar el pago. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MercadoPagoPaymentResponse(
    Long id,
    String status, // "approved", "pending", "rejected", ...
    @JsonProperty("external_reference") String externalReference // el id de nuestra Venta
) {
    public boolean aprobado() {
        return "approved".equalsIgnoreCase(status);
    }
}
