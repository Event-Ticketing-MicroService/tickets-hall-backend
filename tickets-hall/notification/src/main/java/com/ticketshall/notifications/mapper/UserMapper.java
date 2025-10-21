package com.ticketshall.notifications.mapper;

import org.mapstruct.Mapper;

import com.ticketshall.notifications.entity.User;
import com.ticketshall.notifications.mq.events.UserCreatedEvent;
import com.ticketshall.notifications.mq.events.UserUpdatedEvent;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreatedEvent event);
    User toUser(UserUpdatedEvent event);
    UserCreatedEvent toUserCreatedEvent(User User);
}
