package com.refugio.tienda.tienda_service.config;

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

    public static final String EXCHANGE_PEDIDOS = "exchange.pedidos";
    public static final String QUEUE_PEDIDOS_PAGADOS = "cola.pedidos.pagados";
    public static final String ROUTING_KEY_PEDIDOS = "ruta.pedidos.pagados";

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

    public static final String EXCHANGE_RECETAS = "receta.exchange";
    public static final String QUEUE_RECETAS = "receta.queue";
    public static final String ROUTING_KEY_RECETAS = "receta.generada";

    @Bean
    public TopicExchange exchangeRecetas() {
        return new TopicExchange(EXCHANGE_RECETAS);
    }

    @Bean
    public Queue colaRecetas() {
        return new Queue(QUEUE_RECETAS, true);
    }

    @Bean
    public Binding bindingRecetas(Queue colaRecetas, TopicExchange exchangeRecetas) {
        return BindingBuilder.bind(colaRecetas).to(exchangeRecetas).with(ROUTING_KEY_RECETAS);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        // Se simplifica eliminando la propiedad obsoleta TypePrecedence
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                          MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
