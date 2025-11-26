package com.ticketshall.auth.mq.consumers;

import com.ticketshall.auth.Enums.Role;
import com.ticketshall.auth.Enums.UserType;
import com.ticketshall.auth.constants.GeneralConstants;
import com.ticketshall.auth.model.InboxMessage;
import com.ticketshall.auth.model.UserCredentials;
import com.ticketshall.auth.model.id.InboxMessageId;
import com.ticketshall.auth.mq.events.WorkerCreatedMessage;
import com.ticketshall.auth.repository.InboxRepository;
import com.ticketshall.auth.repository.UserCredentialsRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VenueWorkerConsumer {
    private final InboxRepository inboxRepository;
    private final UserCredentialsRepo userCredentialsRepo;
    private final PasswordEncoder passwordEncoder;

    @RabbitListener(queues = "${app.rabbitmq.queues.workerCreated}")
    @Transactional
    public void handleWorkerCreatedMessage(WorkerCreatedMessage message) {
        InboxMessageId inboxMessageId = new InboxMessageId(message.email(), GeneralConstants.WORKER_CREATED_INBOX_TYPE);
        if (inboxRepository.existsById(inboxMessageId)) {
            return;
        }
        UserCredentials userCredentials = UserCredentials
                .builder()
                .userType(UserType.WORKER)
                .role(Role.WORKER)
                .email(message.email())
                .password(passwordEncoder.encode(message.password()))
                .build();
        userCredentialsRepo.save(userCredentials);
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
