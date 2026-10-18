package com.app.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Factura escucha el MISMO exchange/routing key que ya publica MS Ventas
 * para "venta confirmada" (exchange "venta.eventos", routing key
 * "venta.confirmada" — ver RabbitMQConfig de Ventas), pero con su PROPIA
 * cola. Un exchange topic puede tener varias colas bindeadas a la misma
 * routing key: Email y Factura reciben cada uno su propia copia del mismo
 * mensaje, sin que Ventas tenga que saber que existen los dos.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "venta.eventos";
    public static final String CONFIRMADA_ROUTING_KEY = "venta.confirmada";
    public static final String FACTURA_QUEUE = "cola.factura";

    @Bean
    public Queue facturaQueue() {
        return new Queue(FACTURA_QUEUE, true);
    }

    @Bean
    public TopicExchange ventaExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding facturaBinding(Queue facturaQueue, TopicExchange ventaExchange) {
        return BindingBuilder.bind(facturaQueue).to(ventaExchange).with(CONFIRMADA_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
