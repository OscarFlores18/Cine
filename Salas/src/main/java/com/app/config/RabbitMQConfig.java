package com.app.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_BUTACAS = "queue.butacas.actualizar";
    public static final String EXCHANGE_VENTAS = "exchange.ventas";
    public static final String ROUTING_KEY_BUTACAS = "routing.butacas";

    @Bean
    public Queue queueButacas() {
        return new Queue(QUEUE_BUTACAS, true);
    }

    @Bean
    public DirectExchange exchangeVentas() {
        return new DirectExchange(EXCHANGE_VENTAS);
    }

    @Bean
    public Binding bindingButacas(Queue queueButacas, DirectExchange exchangeVentas) {
        return BindingBuilder.bind(queueButacas).to(exchangeVentas).with(ROUTING_KEY_BUTACAS);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}