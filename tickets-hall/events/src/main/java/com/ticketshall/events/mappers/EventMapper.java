package com.ticketshall.events.mappers;

import com.ticketshall.events.dtos.params.CreateEventParams;
import com.ticketshall.events.dtos.responses.EventDTO;
import com.ticketshall.events.models.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface EventMapper {
    @Mapping(source = "totalAvailableTickets", target = "totalAvailableTickets") // mapper fails to map this field implicitly!
    Event toEvent(CreateEventParams createEventParams);

    EventDTO toEventDTO(Event event);
}
