package com.app.client;

import com.app.config.MercadoPagoFeignConfig;
import com.app.dto.MercadoPagoPaymentResponse;
import com.app.dto.MercadoPagoPreferenceRequest;
import com.app.dto.MercadoPagoPreferenceResponse;
import com.app.dto.MercadoPagoRefundResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente Feign hacia la API REAL de Mercado Pago (Checkout Pro). A
 * diferencia de SalaClient/ProductoClient, esto NO pasa por Eureka ni por el
 * Gateway: Mercado Pago es un servicio externo, por eso se declara con
 * "url" fijo (configurable por property, para poder apuntar a sandbox).
 *
 * Hace falta cargar mercadopago.access-token (ver MercadoPagoFeignConfig)
 * con un access token real para que esto funcione — de la misma forma que
 * Gmail necesita credenciales reales de una cuenta de Gmail
 * (spring.mail.username/password) para poder mandar mails.
 */
@FeignClient(
        name = "mercadoPago",
        url = "${mercadopago.api-url:https://api.mercadopago.com}",
        configuration = MercadoPagoFeignConfig.class
)
public interface MercadoPagoClient {

    @PostMapping("/checkout/preferences")
    MercadoPagoPreferenceResponse crearPreferencia(@RequestBody MercadoPagoPreferenceRequest request);

    @GetMapping("/v1/payments/{id}")
    MercadoPagoPaymentResponse obtenerPago(@PathVariable("id") String id);

    @PostMapping("/v1/payments/{id}/refunds")
    MercadoPagoRefundResponse reembolsar(@PathVariable("id") String id);
}
