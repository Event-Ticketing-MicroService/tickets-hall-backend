package com.ticketshall.notifications.mq.consumers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.ticketshall.notifications.constants.GeneralConstants;
import com.ticketshall.notifications.entity.InboxMessage;
import com.ticketshall.notifications.entity.id.InboxMessageId;
import com.ticketshall.notifications.repository.InboxRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.google.zxing.WriterException;
import com.ticketshall.notifications.entity.Event;
import com.ticketshall.notifications.entity.User;
import com.ticketshall.notifications.mq.events.TicketCreatedEvent;
import com.ticketshall.notifications.repository.EventRepository;
import com.ticketshall.notifications.repository.UserRepository;
import com.ticketshall.notifications.service.EmailService;
import com.ticketshall.notifications.service.QrCodeService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketConsumer {
    
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final QrCodeService qrCodeService;
    private final InboxRepository inboxRepository;

    @RabbitListener(queues = "${app.rabbitmq.queues.ticketCreated}")
    public void handleTicketCreated(TicketCreatedEvent ticketCreatedEvent) throws WriterException, IOException, MessagingException {
        InboxMessageId inboxMessageId = new  InboxMessageId(ticketCreatedEvent.id(), GeneralConstants.TICKET_CREATED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        User user = userRepository.findById(ticketCreatedEvent.userId()).orElseThrow();

        Event event = eventRepository.findById(ticketCreatedEvent.eventId()).orElseThrow();

        Map<String,Object> variables = new HashMap<>();
        variables.put("name", user.getFirstName());
        variables.put("eventName", event.getTitle());
        variables.put("startTime", event.getStartsAtUtc());
        variables.put("endTime", event.getEndsAtUtc());
        variables.put("location", event.getLocation());

        String code = ticketCreatedEvent.code();
        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(code, 150, 150);
        variables.put("qrCode", qrCodeBase64);

        emailService.sendTemplate("ticket-created", user.getEmail(), "Ticket for " + event.getTitle(), variables);
        saveInboxRecord(inboxMessageId);
    }
    private void saveInboxRecord(InboxMessageId id) {
        InboxMessage message = InboxMessage.builder()
                .inboxMessageId(id)
                .receivedAt(LocalDateTime.now())
                .build();
        inboxRepository.save(message);
    }
}
