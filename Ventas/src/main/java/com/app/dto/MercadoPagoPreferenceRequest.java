package com.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Body de POST /checkout/preferences de la API real de Mercado Pago.
 * Los nombres van en snake_case (@JsonProperty) porque asi los espera la API.
 *
 * NO se manda "auto_return": a pesar del nombre, Mercado Pago exige que
 * back_urls.success sea una URL PUBLICA real para poder usarlo, y rechaza
 * la preferencia entera (400 "auto_return invalid. back_url.success must
 * be defined" — mensaje enganoso, el campo si esta definido) si es
 * localhost. back_urls solo (sin auto_return) sigue funcionando con
 * localhost: en la pantalla de exito de MP aparece el boton "Volver al
 * sitio", simplemente no es automatico.
 * @JsonInclude(NON_NULL) es lo que hace que, al no mandar autoReturn, el
 * campo "auto_return" no aparezca en el JSON (en vez de ir como null).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MercadoPagoPreferenceRequest(
    List<Item> items,
    @JsonProperty("external_reference") String externalReference,
    @JsonProperty("notification_url") String notificationUrl,
    @JsonProperty("back_urls") BackUrls backUrls,
    @JsonProperty("auto_return") String autoReturn
) {
    public record Item(
        String title,
        Integer quantity,
        @JsonProperty("unit_price") BigDecimal unitPrice
    ) {}

    // A donde vuelve el navegador del comprador segun como termino el pago.
    // Esto es un redirect en SU navegador, no una llamada de MP a nuestro
    // servidor — por eso, a diferencia de notification_url, funciona con
    // localhost sin necesitar un tunel publico.
    public record BackUrls(
        String success,
        String failure,
        String pending
    ) {}
}
