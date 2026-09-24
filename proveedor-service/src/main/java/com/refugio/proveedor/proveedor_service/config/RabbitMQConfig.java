package com.refugio.proveedor.proveedor_service.config;

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

    // ---- Entrada: escucha los pedidos pagados que publica tienda-service ----
    public static final String EXCHANGE_PEDIDOS = "exchange.pedidos";
    public static final String QUEUE_PEDIDOS_PAGADOS = "cola.pedidos.pagados";
    public static final String ROUTING_KEY_PEDIDOS = "ruta.pedidos.pagados";

    // ---- Salida: publica cada cambio de estado del envío ----
    public static final String EXCHANGE_SEGUIMIENTO = "exchange.seguimiento";
    public static final String QUEUE_SEGUIMIENTO = "cola.seguimiento";
    public static final String ROUTING_KEY_SEGUIMIENTO = "ruta.seguimiento";

    @Bean
    public TopicExchange exchangePedidos() {
        return new TopicExchange(EXCHANGE_PEDIDOS);
    }

    @Bean
    public Queue colaPedidosPagados() {
        return new Queue(QUEUE_PEDIDOS_PAGADOS, true);
    }

    @Bean
    public Binding bindingPedidos(Queue colaPedidosPagados, TopicExchange exchangePedidos) {
        return BindingBuilder.bind(colaPedidosPagados).to(exchangePedidos).with(ROUTING_KEY_PEDIDOS);
    }

    @Bean
    public TopicExchange exchangeSeguimiento() {
        return new TopicExchange(EXCHANGE_SEGUIMIENTO);
    }

    @Bean
    public Queue colaSeguimiento() {
        return new Queue(QUEUE_SEGUIMIENTO, true);
    }

    @Bean
    public Binding bindingSeguimiento(Queue colaSeguimiento, TopicExchange exchangeSeguimiento) {
        return BindingBuilder.bind(colaSeguimiento).to(exchangeSeguimiento).with(ROUTING_KEY_SEGUIMIENTO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        // Se simplifica la creación eliminando la configuración de precedencia obsoleta
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
