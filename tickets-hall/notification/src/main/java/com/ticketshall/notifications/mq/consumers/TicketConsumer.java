package com.ticketshall.notifications.mq.consumers;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.ticketshall.notifications.entity.Event;
import com.ticketshall.notifications.entity.User;
import com.ticketshall.notifications.mq.events.TicketCreatedEvent;
import com.ticketshall.notifications.repository.EventRepository;
import com.ticketshall.notifications.repository.UserRepository;
import com.ticketshall.notifications.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketConsumer {
    
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @RabbitListener(queues = "${app.rabbitmq.queues.ticketCreated}")
    public void handleTicketCreated(TicketCreatedEvent ticketCreatedEvent) {
        User user = userRepository.findById(ticketCreatedEvent.userId()).orElseThrow();

        Event event = eventRepository.findById(ticketCreatedEvent.eventId()).orElseThrow();

        
    }

}
