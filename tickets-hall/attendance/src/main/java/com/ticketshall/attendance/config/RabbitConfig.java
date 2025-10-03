package com.ticketshall.attendance.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
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

//    @Value("${app.rabbitmq.exchanges.ticketRetry}")
//    private String ticketRetryExchangeName;

    @Bean
    DirectExchange userExchange() {
        return new DirectExchange(userExchangeName);
    }

    @Bean
    Queue userCreatedQueue() {
        return new Queue(userCreatedQueueName, true);
    }

    @Bean
    Binding userCreatedBinding() {
        return BindingBuilder
                .bind(userCreatedQueue())
                .to(userExchange())
                .with(userCreatedRoutingKey);
    }

    @Bean
    DirectExchange eventExchange() {
        return new DirectExchange(eventExchangeName);
    }

    @Bean
    Queue eventCreatedQueue() {
        return new Queue(eventCreatedQueueName, true);
    }

    @Bean
    Binding eventCreatedBinding() {
        return BindingBuilder
                .bind(eventCreatedQueue())
                .to(eventExchange())
                .with(eventCreatedRoutingKey);
    }

    @Bean
    DirectExchange ticketExchange() {
        return new DirectExchange(ticketExchangeName);
    }

//    @Bean("ticketRetryExchange")
//    DirectExchange ticketRetryExchange() {
//        return new DirectExchange(ticketRetryExchangeName);
//    }

    @Bean
    Queue ticketCreatedQueue() {
        return new Queue(ticketCreatedQueueName, true);
    }

    @Bean
    Binding ticketCreatedBinding() {
        return BindingBuilder.bind(ticketCreatedQueue())
                .to(ticketExchange())
                .with(ticketCreatedRoutingKey);
    }

//    @Bean
//    Binding ticketRetryBinding(@Qualifier("ticketCreatedQueue") Queue ticketCreatedQueue,
//            @Qualifier("ticketRetryExchange") DirectExchange ticketRetryExchange) {
//        return BindingBuilder.bind(ticketCreatedQueue)
//                .to(ticketRetryExchange)
//                .with(ticketCreatedRoutingKey);
//    }

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
