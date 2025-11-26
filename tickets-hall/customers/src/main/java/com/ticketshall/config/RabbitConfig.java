package com.ticketshall.config;

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

    @Value("${app.rabbitmq.exchanges.customer}")
    private String customerExchangeName;
    @Value("${app.rabbitmq.exchanges.dead-letter}")
    private String deadLetterExchangeName;
    @Value("${app.rabbitmq.routing.dead-letter}")
    private String deadLetterRoutingKey;
    @Value("${app.rabbitmq.queues.dead-letter}")
    private String deadLetterQueueName;

    @Bean
    DirectExchange customerExchange() {
        return new DirectExchange(customerExchangeName);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchangeName);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueueName).build();
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(deadLetterRoutingKey);
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

