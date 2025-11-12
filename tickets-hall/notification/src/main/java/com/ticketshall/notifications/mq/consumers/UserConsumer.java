package com.ticketshall.notifications.mq.consumers;

import java.time.LocalDateTime;
import java.util.Map;

import com.ticketshall.notifications.constants.GeneralConstants;
import com.ticketshall.notifications.entity.InboxMessage;
import com.ticketshall.notifications.entity.id.InboxMessageId;
import com.ticketshall.notifications.repository.InboxRepository;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.ticketshall.notifications.mapper.UserMapper;
import com.ticketshall.notifications.mq.events.UserCreatedEvent;
import com.ticketshall.notifications.mq.events.UserUpdatedEvent;
import com.ticketshall.notifications.repository.UserRepository;
import com.ticketshall.notifications.service.EmailService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserConsumer {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final InboxRepository inboxRepository;

    @RabbitListener(queues = "${app.rabbitmq.queues.userCreated}")
    @Transactional
    public void handleUserCreated(UserCreatedEvent userCreatedEvent) throws MessagingException
    {
        InboxMessageId inboxMessageId = new InboxMessageId(userCreatedEvent.id(), GeneralConstants.USER_CREATED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        userRepository.save(userMapper.toUser(userCreatedEvent));

        emailService.sendTemplate("welcome-user", 
            userCreatedEvent.email(), 
            "Welcome, " + userCreatedEvent.firstName(),
            Map.of("name", userCreatedEvent.firstName()));
        saveInboxRecord(inboxMessageId);
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.userUpdated}")
    @Transactional
    public void handleUserUpdated(UserUpdatedEvent userUpdatedEvent) throws MessagingException
    {
        InboxMessageId inboxMessageId = new  InboxMessageId(userUpdatedEvent.id(), GeneralConstants.USER_UPDATED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
            var user = userMapper.toUser(userUpdatedEvent);
            userRepository.save(user);
            
            emailService.sendTemplate("profile-updated", 
                userUpdatedEvent.email(), 
                "Profile Updated Successfully",
                Map.of(
                    "name", userUpdatedEvent.firstName(),
                    "firstName", userUpdatedEvent.firstName(),
                    "lastName", userUpdatedEvent.lastName(),
                    "email", userUpdatedEvent.email()
                ));
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
