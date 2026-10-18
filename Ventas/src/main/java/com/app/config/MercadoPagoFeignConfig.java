package com.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;

/**
 * Autenticacion contra la API real de Mercado Pago: un access token fijo en
 * el header Authorization (no es el "X-Gateway-Secret" interno, ver
 * FeignConfig). Hace falta un access token de una cuenta de Mercado Pago
 * (de prueba o real) en mercadopago.access-token para que esto funcione.
 *
 * A PROPOSITO sin @Configuration (ver el comentario en FeignConfig): asi
 * este interceptor SOLO se aplica a MercadoPagoClient, y no se filtra a
 * SalaClient/ProductoClient.
 */
public class MercadoPagoFeignConfig {

    @Bean
    public RequestInterceptor mercadoPagoAuthInterceptor(@Value("${mercadopago.access-token:}") String accessToken) {
        return template -> {
            if (accessToken != null && !accessToken.isBlank()) {
                template.header("Authorization", "Bearer " + accessToken);
            }
        };
    }
}
