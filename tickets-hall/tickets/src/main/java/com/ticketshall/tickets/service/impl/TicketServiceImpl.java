package com.ticketshall.tickets.service.impl;

import com.ticketshall.tickets.dto.CreateTicketTypeRequest;
import com.ticketshall.tickets.dto.UpdateTicketTypeRequest;
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

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final TicketTypeMapper ticketTypeMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public TicketType createTicketType(CreateTicketTypeRequest request) {
        if(!eventRepository.existsById(request.eventId())) {
            throw new IllegalArgumentException("Event not found: " + request.eventId());
        }

        var ticketType = ticketTypeMapper.toTicketType(request);
        ticketTypeRepository.save(ticketType);

        updateEventTicketTypesCache(ticketType.getEventId());

        return ticketType;
    }

    @Override
    public TicketType updateTicketType(UpdateTicketTypeRequest request) {
        var type = ticketTypeRepository.findById(request.id())
                .orElseThrow(() -> new RuntimeException("TicketType not found: " + request.id()));

        type.setName(request.name());
        type.setPrice(request.price());
        type.setTotalStock(request.stock());
        type.setEventId(request.eventId());

        ticketTypeRepository.save(type);
        updateEventTicketTypesCache(type.getEventId());

        return type;
    }

    @Override
    public boolean deleteTicketType(UUID ticketTypeId) {
        var type = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new RuntimeException("TicketType not found: " + ticketTypeId));

        ticketTypeRepository.delete(type);

        updateEventTicketTypesCache(type.getEventId());

        return true;
    }

    @Override
    public List<TicketType> listTicketTypesForEvent(UUID eventId) {
        String cacheKey = getEventTicketTypesKey(eventId);
        List<TicketType> cached = (List<TicketType>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        List<TicketType> ticketTypes = ticketTypeRepository.getTicketTypesByEventId(eventId);
        if(!ticketTypes.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, ticketTypes, CACHE_TTL);
        }

        return ticketTypes;
    }

    private void updateEventTicketTypesCache(UUID eventId) {
        List<TicketType> freshList = ticketTypeRepository.getTicketTypesByEventId(eventId);
        String cacheKey = getEventTicketTypesKey(eventId);

        if(freshList.isEmpty()) {
            redisTemplate.delete(cacheKey);
        } else {
            redisTemplate.opsForValue().set(cacheKey, freshList, CACHE_TTL);
        }
    }
    private String getEventTicketTypesKey(UUID eventId) {
        return GeneralConstants.REDIS_EVENT_PREFIX + eventId + GeneralConstants.REDIS_TICKET_TYPE_INFIX;
    }
}