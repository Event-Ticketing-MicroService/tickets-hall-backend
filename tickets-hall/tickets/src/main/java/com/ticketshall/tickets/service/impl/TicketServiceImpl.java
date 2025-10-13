package com.ticketshall.tickets.service.impl;

import com.ticketshall.tickets.dto.CreateTicketTypeRequest;
import com.ticketshall.tickets.mapper.TicketTypeMapper;
import com.ticketshall.tickets.models.Ticket;
import com.ticketshall.tickets.models.TicketType;
import com.ticketshall.tickets.models.nonStoredModels.constants.GeneralConstants;
import com.ticketshall.tickets.repository.EventRepository;
import com.ticketshall.tickets.repository.TicketTypeRepository;
import com.ticketshall.tickets.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final TicketTypeMapper ticketTypeMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    @Override
    public TicketType createTicketType(CreateTicketTypeRequest request){
        if(!eventRepository.existsById(request.eventId())){
            throw new IllegalArgumentException("Event not found: " + request.eventId()); // TODO: Create Exceptions
        }
        var ticketType = ticketTypeMapper.toTicketType(request);
        ticketTypeRepository.save(ticketType);
        return ticketType;
    }
    // TODO: Keep cache valid throughout various ops on TicketType
    @Override
    public List<TicketType> listTicketTypesForEvent(UUID eventId) {
        var keys = redisTemplate.keys(GeneralConstants.REDIS_EVENT_PREFIX + eventId + GeneralConstants.REDIS_TICKET_TYPE_INFIX + "*");
        List<TicketType> result;
        if (keys.isEmpty()) { // cache miss
            result = ticketTypeRepository.getTicketTypesByEventId(eventId);
            result.forEach(ticketType -> redisTemplate.opsForValue()
                    .set(GeneralConstants.REDIS_EVENT_PREFIX + eventId + GeneralConstants.REDIS_TICKET_TYPE_INFIX + ticketType.getId(), ticketType));
            return result;
        }
        result = new ArrayList<>();
        for (String key: keys){
            var ticketType = (TicketType) redisTemplate.opsForValue().get(key);
            if (ticketType != null) {
                result.add(ticketType);
            }
        }
        return result;
    }

}
