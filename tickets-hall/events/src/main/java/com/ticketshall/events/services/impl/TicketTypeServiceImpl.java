package com.ticketshall.events.services.impl;

import com.ticketshall.events.dtos.params.CreateTicketTypeParams;
import com.ticketshall.events.exceptions.BadRequestException;
import com.ticketshall.events.exceptions.ConflictErrorException;
import com.ticketshall.events.exceptions.NotFoundException;
import com.ticketshall.events.mappers.TicketTypeMapper;
import com.ticketshall.events.models.Event;
import com.ticketshall.events.models.TicketType;
import com.ticketshall.events.repositories.EventRepository;
import com.ticketshall.events.services.TicketTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketTypeServiceImpl implements TicketTypeService {

    private final EventRepository eventRepository;
    private final TicketTypeMapper ticketTypeMapper;
    public TicketTypeServiceImpl(EventRepository eventRepository, TicketTypeMapper ticketTypeMapper) {
        this.eventRepository = eventRepository;
        this.ticketTypeMapper = ticketTypeMapper;
    }

    @Override
    public List<TicketType> createTicketTypes(UUID eventId, List<CreateTicketTypeParams> ticketTypesParamsList) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (eventOptional.isEmpty()) throw new NotFoundException("event with given id is not found");

        Event event = eventOptional.get();
        if (event.getIsPublished()) throw new ConflictErrorException("Can't add ticket types for a published event");

        List<TicketType> existingTicketTypes = event.getTicketTypes();
        ticketTypesParamsList.forEach(tt -> {
            tt.setName(tt.getName().toLowerCase());
            if(existingTicketTypes.stream().anyMatch(existing -> existing.getName().equalsIgnoreCase(tt.getName()))) throw new ConflictErrorException("an event with name '" + tt.getName() + "' exists");
        });



        int newTotalTickets = ticketTypesParamsList.stream().mapToInt(CreateTicketTypeParams::getQuantity).sum();
        int oldTotalTickets = existingTicketTypes.stream().mapToInt(TicketType::getQuantity).sum();

        if (oldTotalTickets+newTotalTickets > event.getTotalAvailableTickets())
            throw new BadRequestException("New Sum of ticket quantities does not match event's max available tickets");


        List<TicketType> ticketTypes = ticketTypeMapper.toTicketTypeList(ticketTypesParamsList);
        ticketTypes.forEach(t -> t.setEventId(eventId));

        existingTicketTypes.addAll(ticketTypes);
        ticketTypes = eventRepository.save(event).getTicketTypes().stream().filter(t1 -> ticketTypesParamsList.stream().anyMatch(t2 -> t1.getName().equalsIgnoreCase(t2.getName()))).toList();

        return ticketTypes;
    }
}
