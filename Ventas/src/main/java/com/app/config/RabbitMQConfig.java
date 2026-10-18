package com.app.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ del microservicio EMISOR (Ventas).
 *
 * Nombres de exchange/routing key/cola calcados EXACTAMENTE del diagrama
 * "Broker — RabbitMQ: venta confirmada" del Figma del proyecto:
 *
 *   VentaEventPublisher (MS Venta) --publica--> Exchange "venta.eventos" (topic)
 *     --bind: venta.confirmada--> Queue "cola.confirmacion-venta" --@RabbitListener--> MS Email
 *
 * Venta no espera respuesta: publica y sigue. Si MS Email esta caido, el
 * mensaje queda en la cola hasta que vuelva a levantar — no se pierde la
 * notificacion.
 *
 * El Figma solo dibuja el caso "venta confirmada"; la cola/routing key de
 * "venta cancelada" es una extension propia siguiendo el mismo patron
 * (mismo exchange "venta.eventos", routing key "venta.cancelada"), para que
 * el equipo de Email tambien pueda mandar el mail de cancelacion si quiere.
 *
 * IMPORTANTE para el equipo: estos nombres son el "contrato" con MS Email.
 * Quien implemente el consumer en Email tiene que declarar EXACTAMENTE los
 * mismos nombres de exchange/cola/routing key (ver CONTRATOS-REQUERIDOS.md).
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "venta.eventos";

    public static final String CONFIRMADA_QUEUE = "cola.confirmacion-venta";
    public static final String CONFIRMADA_ROUTING_KEY = "venta.confirmada";

    // Extension propia (no dibujada en el Figma), mismo patron que arriba.
    public static final String CANCELADA_QUEUE = "cola.cancelacion-venta";
    public static final String CANCELADA_ROUTING_KEY = "venta.cancelada";

    // --- Contrato REAL (no del Figma) con MS Sala para ocupar/liberar
    // butacas: Sala es la DUEÑA del exchange/cola/binding (los declara en su
    // propio RabbitMQConfig); Ventas solo necesita publicar contra el mismo
    // nombre de exchange + routing key. Declarar el mismo DirectExchange
    // acá (misma clase, mismo nombre) es idempotente — si Sala ya lo creó
    // en el broker, esto no lo duplica ni lo rompe — y deja a Ventas
    // publicar aunque arranque antes que Sala.
    public static final String EXCHANGE_VENTAS_SALA = "exchange.ventas";
    public static final String ROUTING_KEY_BUTACAS = "routing.butacas";

    @Bean
    public DirectExchange exchangeVentasSala() {
        return new DirectExchange(EXCHANGE_VENTAS_SALA);
    }

    // Colas duraderas: sobreviven a un reinicio del broker
    @Bean
    public Queue ventaConfirmadaQueue() {
        return new Queue(CONFIRMADA_QUEUE, true);
    }

    @Bean
    public Queue ventaCanceladaQueue() {
        return new Queue(CANCELADA_QUEUE, true);
    }

    @Bean
    public TopicExchange ventaExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding ventaConfirmadaBinding(Queue ventaConfirmadaQueue, TopicExchange ventaExchange) {
        return BindingBuilder.bind(ventaConfirmadaQueue).to(ventaExchange).with(CONFIRMADA_ROUTING_KEY);
    }

    @Bean
    public Binding ventaCanceladaBinding(Queue ventaCanceladaQueue, TopicExchange ventaExchange) {
        return BindingBuilder.bind(ventaCanceladaQueue).to(ventaExchange).with(CANCELADA_ROUTING_KEY);
    }

    // Convierte automaticamente los objetos Java a JSON al enviarlos.
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
