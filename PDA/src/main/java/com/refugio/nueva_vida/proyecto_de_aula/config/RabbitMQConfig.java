package com.refugio.nueva_vida.proyecto_de_aula.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ============================================================
    // NOTIFICACIONES DEL REFUGIO
    // ============================================================

    public static final String EXCHANGE_NAME = "exchange.refugio";
    public static final String QUEUE_NAME = "cola.notificaciones";
    public static final String ROUTING_KEY = "ruta.notificaciones";


    // ============================================================
    // PROCESAMIENTO DE FOTOS
    // ============================================================

    public static final String EXCHANGE_FOTOS = "exchange.fotos";
    public static final String QUEUE_FOTOS = "cola.fotos.procesamiento";
    public static final String ROUTING_KEY_FOTOS = "ruta.fotos";


    // ============================================================
    // SEGUIMIENTO DE PEDIDOS (viene de proveedor-service)
    // ============================================================

    public static final String EXCHANGE_SEGUIMIENTO = "exchange.seguimiento";
    public static final String QUEUE_SEGUIMIENTO = "cola.seguimiento";
    public static final String ROUTING_KEY_SEGUIMIENTO = "ruta.seguimiento";


    // ============================================================
    // SALUD / RECETAS
    // ============================================================

    public static final String QUEUE_SALUD = "cola.salud";
    public static final String ROUTING_KEY_SALUD = "ruta.salud";


    // ============================================================
    // 🆕 RECETAS → TIENDA (PDA publica, tienda escucha)
    // ============================================================

    public static final String EXCHANGE_RECETAS    = "receta.exchange";
    public static final String QUEUE_RECETAS       = "receta.queue";
    public static final String ROUTING_KEY_RECETAS = "receta.generada";


    // ============================================================
    // EXCHANGE DE NOTIFICACIONES
    // ============================================================

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }


    // ============================================================
    // COLA DE NOTIFICACIONES
    // ============================================================

    @Bean
    public Queue notificacionesQueue() {
        return new Queue(QUEUE_NAME, true);
    }


    // ============================================================
    // BINDING NOTIFICACIONES
    // ============================================================

    @Bean
    public Binding binding(
            Queue notificacionesQueue,
            TopicExchange topicExchange
    ) {
        return BindingBuilder
                .bind(notificacionesQueue)
                .to(topicExchange)
                .with(ROUTING_KEY);
    }


    // ============================================================
    // COLA DE SALUD / RECETAS
    // ============================================================

    @Bean
    public Queue saludQueue() {
        return new Queue(QUEUE_SALUD, true);
    }


    // ============================================================
    // BINDING NOTIFICACIONES DE SALUD
    // ============================================================

    @Bean
    public Binding saludBinding(
            Queue saludQueue,
            TopicExchange topicExchange
    ) {
        return BindingBuilder
                .bind(saludQueue)
                .to(topicExchange)
                .with(ROUTING_KEY_SALUD);
    }


    // ============================================================
    // EXCHANGE DE FOTOS
    // ============================================================

    @Bean
    public TopicExchange fotosExchange() {
        return new TopicExchange(EXCHANGE_FOTOS);
    }


    // ============================================================
    // COLA DE FOTOS
    // ============================================================

    @Bean
    public Queue fotosQueue() {
        return new Queue(QUEUE_FOTOS, true);
    }


    // ============================================================
    // BINDING DE FOTOS
    // ============================================================

    @Bean
    public Binding fotosBinding(
            Queue fotosQueue,
            TopicExchange fotosExchange
    ) {
        return BindingBuilder
                .bind(fotosQueue)
                .to(fotosExchange)
                .with(ROUTING_KEY_FOTOS);
    }


    // ============================================================
    // EXCHANGE DE SEGUIMIENTO
    // ============================================================

    @Bean
    public TopicExchange seguimientoExchange() {
        return new TopicExchange(EXCHANGE_SEGUIMIENTO);
    }


    // ============================================================
    // COLA DE SEGUIMIENTO
    // ============================================================

    @Bean
    public Queue seguimientoQueue() {
        return new Queue(QUEUE_SEGUIMIENTO, true);
    }


    // ============================================================
    // BINDING DE SEGUIMIENTO
    // ============================================================

    @Bean
    public Binding seguimientoBinding(
            Queue seguimientoQueue,
            TopicExchange seguimientoExchange
    ) {
        return BindingBuilder
                .bind(seguimientoQueue)
                .to(seguimientoExchange)
                .with(ROUTING_KEY_SEGUIMIENTO);
    }


    // ============================================================
    // 🆕 EXCHANGE DE RECETAS
    // ============================================================

    @Bean
    public TopicExchange recetasExchange() {
        return new TopicExchange(EXCHANGE_RECETAS);
    }


    // ============================================================
    // 🆕 COLA DE RECETAS
    // ============================================================

    @Bean
    public Queue recetasQueue() {
        return new Queue(QUEUE_RECETAS, true);
    }


    // ============================================================
    // 🆕 BINDING DE RECETAS
    // ============================================================

    @Bean
    public Binding recetasBinding(
            Queue recetasQueue,
            TopicExchange recetasExchange
    ) {
        return BindingBuilder
                .bind(recetasQueue)
                .to(recetasExchange)
                .with(ROUTING_KEY_RECETAS);
    }


    // ============================================================
    // CONVERSOR JSON
    // ============================================================

    @Bean
    public MessageConverter jsonMessageConverter() {
        // Se elimina TypePrecedence obsoleto; Spring Boot 3 infiere los tipos automáticamente
        return new Jackson2JsonMessageConverter();
    }


    // ============================================================
    // RABBIT TEMPLATE
    // ============================================================

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
