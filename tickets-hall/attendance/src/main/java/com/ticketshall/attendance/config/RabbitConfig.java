package com.ticketshall.attendance.config;

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

    @Value("${app.rabbitmq.exchanges.user}")
    private String userExchangeName;

    @Value("${app.rabbitmq.routing.userCreated}")
    private String userCreatedRoutingKey;

    @Value("${app.rabbitmq.queues.userCreated}")
    private String userCreatedQueueName;

    @Value("${app.rabbitmq.exchanges.event}")
    private String eventExchangeName;

    @Value("${app.rabbitmq.routing.eventCreated}")
    private String eventCreatedRoutingKey;

    @Value("${app.rabbitmq.queues.eventCreated}")
    private String eventCreatedQueueName;

    @Value("${app.rabbitmq.exchanges.ticket}")
    private String ticketExchangeName;

    @Value("${app.rabbitmq.routing.ticketCreated}")
    private String ticketCreatedRoutingKey;

    @Value("${app.rabbitmq.queues.ticketCreated}")
    private String ticketCreatedQueueName;

    @Value("${app.rabbitmq.exchanges.dead-letter}")
    private String deadLetterExchangeName;

    @Value("${app.rabbitmq.routing.dead-letter}")
    private String deadLetterRoutingKey;

    @Value("${app.rabbitmq.queues.dead-letter}")
    private String deadLetterQueueName;

    @Bean
    DirectExchange userExchange() {
        return new DirectExchange(userExchangeName);
    }

    @Bean
    Queue userCreatedQueue() {
        return QueueBuilder.durable(userCreatedQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    @Bean
    Binding userCreatedBinding() {
        return BindingBuilder.bind(userCreatedQueue())
                .to(userExchange())
                .with(userCreatedRoutingKey);
    }

    @Bean
    DirectExchange eventExchange() {
        return new DirectExchange(eventExchangeName);
    }

    @Bean
    Queue eventCreatedQueue() {
        return QueueBuilder.durable(eventCreatedQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    @Bean
    Binding eventCreatedBinding() {
        return BindingBuilder.bind(eventCreatedQueue())
                .to(eventExchange())
                .with(eventCreatedRoutingKey);
    }

    @Bean
    DirectExchange ticketExchange() {
        return new DirectExchange(ticketExchangeName);
    }

    @Bean
    Queue ticketCreatedQueue() {
        return QueueBuilder.durable(ticketCreatedQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    @Bean
    Binding ticketCreatedBinding() {
        return BindingBuilder.bind(ticketCreatedQueue())
                .to(ticketExchange())
                .with(ticketCreatedRoutingKey);
    }

    // Final DLQ
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
