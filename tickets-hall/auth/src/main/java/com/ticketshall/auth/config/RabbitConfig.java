package com.ticketshall.auth.config;

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

    @Value("${app.rabbitmq.exchanges.worker}")
    private String workerExchangeName;

    @Value("${app.rabbitmq.routing.workerCreated}")
    private String workerCreatedRoutingKey;

    @Value("${app.rabbitmq.routing.workerUpdated}")
    private String workerUpdatedRoutingKey;

    @Value("${app.rabbitmq.routing.workerDeleted}")
    private String workerDeletedRoutingKey;

    @Value("${app.rabbitmq.queues.workerCreated}")
    private String workerCreatedQueueName;

    @Value("${app.rabbitmq.queues.workerUpdated}")
    private String workerUpdatedQueueName;

    @Value("${app.rabbitmq.queues.workerDeleted}")
    private String workerDeletedQueueName;

    @Value("${app.rabbitmq.exchanges.dead-letter}")
    private String deadLetterExchangeName;

    @Value("${app.rabbitmq.routing.dead-letter}")
    private String deadLetterRoutingKey;

    @Value("${app.rabbitmq.queues.dead-letter}")
    private String deadLetterQueueName;

    @Bean
    DirectExchange workerExchange() {
        return new DirectExchange(workerExchangeName);
    }

    @Bean
    Queue workerCreatedQueue() {
        return QueueBuilder.durable(workerCreatedQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    @Bean
    Binding workerCreatedBinding() {
        return BindingBuilder.bind(workerCreatedQueue())
                .to(workerExchange())
                .with(workerCreatedRoutingKey);
    }

    @Bean
    Queue workerUpdatedQueue() {
        return QueueBuilder.durable(workerUpdatedQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    @Bean
    Binding workerUpdatedBinding() {
        return BindingBuilder.bind(workerUpdatedQueue())
                .to(workerExchange())
                .with(workerUpdatedRoutingKey);
    }

    @Bean
    Queue workerDeletedQueue() {
        return QueueBuilder.durable(workerDeletedQueueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchangeName)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    @Bean
    Binding workerDeletedBinding() {
        return BindingBuilder.bind(workerDeletedQueue())
                .to(workerExchange())
                .with(workerDeletedRoutingKey);
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
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

}
