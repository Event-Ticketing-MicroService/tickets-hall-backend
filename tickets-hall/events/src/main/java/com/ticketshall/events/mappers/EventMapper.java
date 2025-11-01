package com.ticketshall.events.mappers;

import com.ticketshall.events.dtos.messages.EventDeletedMessage;
import com.ticketshall.events.dtos.messages.EventUpsertedMessage;
import com.ticketshall.events.dtos.params.UpsertEventParams;
import com.ticketshall.events.dtos.responses.EventDTO;
import com.ticketshall.events.models.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface EventMapper {
    @Mapping(source = "totalAvailableTickets", target = "totalAvailableTickets") // mapper fails to map this field implicitly!
    Event toEvent(UpsertEventParams UpsertEventParams);

    EventDTO toEventDTO(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "isPublished", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    void updateEventFromUpsertParams(UpsertEventParams upsertEventParams, @MappingTarget Event event);



    EventDeletedMessage toEventDeletedMessage(Event event);

    @Mapping(target = "startsAtUtc", source = "startsAt")
    @Mapping(target = "endsAtUtc", source = "endsAt")
    EventUpsertedMessage toEventUpsertedMessage(Event event);
}
