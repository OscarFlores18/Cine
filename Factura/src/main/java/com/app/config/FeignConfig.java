package com.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;

/**
 * Los microservicios rechazan (403) todo lo que no traiga la cabecera
 * X-Gateway-Secret (ver GatewayInterceptor). Cuando Ventas le pega a Sala o
 * a Productos por Feign (sin pasar por el Gateway) tiene que mandarla ella
 * misma. El secreto es el mismo gateway.internal-secret que viene del
 * Config Server.
 *
 * A PROPOSITO sin @Configuration: si la llevara, Spring la tomaria como
 * default para TODOS los @FeignClient (incluido MercadoPagoClient), y
 * terminariamos mandandole nuestra cabecera interna a la API de Mercado
 * Pago. Por eso se referencia a mano con @FeignClient(configuration = ...)
 * solo en SalaClient y ProductoClient.
 */
public class FeignConfig {

    @Bean
    public RequestInterceptor gatewaySecretInterceptor(@Value("${gateway.internal-secret}") String secret) {
        return template -> template.header("X-Gateway-Secret", secret);
    }
}
