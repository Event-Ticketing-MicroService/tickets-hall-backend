package com.ticketshall.events.mappers;

import com.ticketshall.events.dtos.params.UpsertEventParams;
import com.ticketshall.events.dtos.responses.EventDTO;
import com.ticketshall.events.models.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface EventMapper {
    @Mapping(source = "totalAvailableTickets", target = "totalAvailableTickets") // mapper fails to map this field implicitly!
    Event toEvent(UpsertEventParams UpsertEventParams);

    EventDTO toEventDTO(Event event);
}
