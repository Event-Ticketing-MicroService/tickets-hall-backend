package com.ticketshall.notifications.mq.consumers;

import java.util.Map;

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

    @RabbitListener(queues = "${app.rabbitmq.queues.userCreated}")
    public void handleUserCreated(UserCreatedEvent userCreatedEvent) throws MessagingException
    {
        userRepository.save(userMapper.toUser(userCreatedEvent));

        emailService.sendTemplate("welcome-user", 
            userCreatedEvent.email(), 
            "Welcome, " + userCreatedEvent.firstName(),
            Map.of("name", userCreatedEvent.firstName()));
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.userUpdated}")
    public void handleUserUpdated(UserUpdatedEvent userUpdatedEvent) throws MessagingException
    {
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
    }
    
}
