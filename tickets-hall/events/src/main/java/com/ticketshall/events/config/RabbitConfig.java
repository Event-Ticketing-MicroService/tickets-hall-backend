package com.ticketshall.events.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.exchanges.event}")
    private String eventExchangeName;

    // --- ADDED FOR TESTING ---
    @Value("${app.rabbitmq.routing.event-created}")
    private String eventCreatedRoutingKey;

    @Value("${app.rabbitmq.queues.test-events}")
    private String eventsTestQueueName;

    @Value("${app.rabbitmq.queues.test-notifications}")
    private String notificationsTestQueueName;


    @Bean
    DirectExchange eventExchange() {
        return new DirectExchange(eventExchangeName);
    }

    @Bean
    Queue eventsTestQueue() {
        return new Queue(eventsTestQueueName, true);
    }

    @Bean
    Queue notificationsTestQueue() {
        return new Queue(notificationsTestQueueName, true);
    }

    @Bean
    Binding eventsTestBinding() {
        return BindingBuilder
                .bind(eventsTestQueue())
                .to(eventExchange())
                .with(eventCreatedRoutingKey);
    }

    @Bean
    Binding notificationsTestBinding() {
        return BindingBuilder
                .bind(notificationsTestQueue())
                .to(eventExchange())
                .with(eventCreatedRoutingKey);
    }


    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}