package com.ticketshall.tickets.service.impl;

import com.ticketshall.tickets.dto.request.CreateTicketTypeRequest;
import com.ticketshall.tickets.dto.request.UpdateTicketTypeRequest;
import com.ticketshall.tickets.exceptions.TicketTypeNotFoundException;
import com.ticketshall.tickets.mapper.TicketTypeMapper;
import com.ticketshall.tickets.models.TicketType;
import com.ticketshall.tickets.models.nonStoredModels.constants.GeneralConstants;
import com.ticketshall.tickets.repository.EventRepository;
import com.ticketshall.tickets.repository.TicketTypeRepository;
import com.ticketshall.tickets.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final TicketTypeMapper ticketTypeMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    // TODO: make the TTL until the Event is over
    //private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public TicketType createTicketType(CreateTicketTypeRequest request) {
        if(!eventRepository.existsById(request.eventId())) {
            throw new IllegalArgumentException("Event not found: " + request.eventId());
        }

        var ticketType = ticketTypeMapper.toTicketType(request);
        ticketTypeRepository.save(ticketType);
        // AppendToCache don't update the full list
        var cacheKey = getEventTicketTypesKey(request.eventId());
        List<TicketType> cached = (List<TicketType>)redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            cached.add(ticketType);
            redisTemplate.opsForValue().set(cacheKey, cached);
        }
        return ticketType;
    }

    @Override
    public TicketType updateTicketType(UpdateTicketTypeRequest request) {
        // Check Cache First
        // if not found in cache
        // Update Fields In cache
        // Update Fields in Database
        var event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + request.eventId()));
        if (event.getReservationStartsAtUtc().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("TicketType can't be updated because the reservations already started");
        }
        var cacheKey = getEventTicketTypesKey(request.eventId());
        List<TicketType> cached = (List<TicketType>)redisTemplate.opsForValue().get(cacheKey);
        int newAvailableStock;
        if (cached != null && !cached.isEmpty()) {
            for (TicketType type : cached) {
                if (type.getId().equals(request.id())) {
                    newAvailableStock = type.getAvailableStock() - (request.stock() - type.getTotalStock());
                    if(newAvailableStock < 0) {
                        throw new IllegalArgumentException("the new Available Stock can't be negative " + type.getName());
                    }
                    type.setName(request.name());
                    type.setPrice(request.price());
                    type.setTotalStock(request.stock());
                    type.setAvailableStock(newAvailableStock);
                    redisTemplate.opsForValue().set(cacheKey, cached);
                    return type;
                }
            }
        }
        var type = ticketTypeRepository.findById(request.id())
                .orElseThrow(() -> new RuntimeException("TicketType not found: " + request.id()));
        newAvailableStock = type.getAvailableStock() - (request.stock() - type.getTotalStock());
        type.setName(request.name());
        type.setPrice(request.price());
        type.setTotalStock(request.stock());
        type.setAvailableStock(newAvailableStock);
        type.setEventId(request.eventId());
        ticketTypeRepository.save(type);

        return type;
    }

    @Override
    public boolean deleteTicketType(UUID ticketTypeId) {
        // remove from cache
        // remove from DB
        // put constraint not to delete if reservations are open
        var dbType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new TicketTypeNotFoundException("TicketType not found: " + ticketTypeId));
        var event = eventRepository.findById(dbType.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + dbType.getEventId()));
        if (event.getReservationStartsAtUtc().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("TicketType can't be updated because the reservations already started");
        }
        var cacheKey = getEventTicketTypesKey(dbType.getEventId());
        List<TicketType> cached = (List<TicketType>)redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            cached.removeIf(t -> t.getId().equals(ticketTypeId));
            redisTemplate.opsForValue().set(cacheKey, cached);
            return true;
        }

        ticketTypeRepository.delete(dbType);
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
            redisTemplate.opsForValue().set(cacheKey, ticketTypes);
        }

        return ticketTypes;
    }

    private String getEventTicketTypesKey(UUID eventId) {
        return GeneralConstants.REDIS_EVENT_PREFIX + eventId + GeneralConstants.REDIS_TICKET_TYPE_INFIX;
    }
}