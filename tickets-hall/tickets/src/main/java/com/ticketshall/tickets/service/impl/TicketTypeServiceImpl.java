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
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
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
    private final RedissonClient redissonClient;
    // TODO: make the TTL until the Event is over
    //private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public TicketType createTicketType(CreateTicketTypeRequest request) {
        if (!eventRepository.existsById(request.eventId())) {
            throw new IllegalArgumentException("Event not found: " + request.eventId());
        }

        var ticketType = ticketTypeMapper.toTicketType(request);
        ticketType.setEvent(eventRepository.getReferenceById(request.eventId()));
        ticketTypeRepository.save(ticketType);

        // AppendToCache don't update the full list
        var cacheKey = getEventTicketTypesKey(request.eventId());
        RList<TicketType> cachedList = redissonClient.getList(cacheKey);
        if (cachedList.isExists() && !cachedList.isEmpty()) {
            cachedList.add(ticketType);
        }
        return ticketType;
    }

    @Override
    public TicketType updateTicketType(UpdateTicketTypeRequest request) {
        var cacheKey = getEventTicketTypesKey(request.eventId());
        RList<TicketType> cachedList = redissonClient.getList(cacheKey);
        int newAvailableStock;

        if (cachedList.isExists() && !cachedList.isEmpty()) {
            List<TicketType> cached = cachedList.readAll();
            for (int i = 0; i < cached.size(); i++) {
                TicketType type = cached.get(i);
                if (type.getId().equals(request.id())) {
                    if (type.getReservationsStartsAtUtc().isBefore(LocalDateTime.now())) {
                        throw new IllegalArgumentException("TicketType can't be updated because the reservations already started");
                    }
                    newAvailableStock = type.getAvailableStock() + (request.stock() - type.getTotalStock());
                    if (newAvailableStock < 0) {
                        throw new IllegalArgumentException("the new Available Stock can't be negative " + type.getName());
                    }
                    type.setName(request.name());
                    type.setPrice(request.price());
                    type.setTotalStock(request.stock());
                    type.setAvailableStock(newAvailableStock);
                    cachedList.set(i, type);
                    ticketTypeRepository.save(type);
                    return type;
                }
            }
        }
        var type = ticketTypeRepository.findById(request.id())
                .orElseThrow(() -> new RuntimeException("TicketType not found: " + request.id()));
        newAvailableStock = type.getAvailableStock() + (request.stock() - type.getTotalStock());
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
        var dbType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new TicketTypeNotFoundException("TicketType not found: " + ticketTypeId));
        if (dbType.getReservationsStartsAtUtc().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("TicketType can't be deleted because the reservations already started");
        }

        var cacheKey = getEventTicketTypesKey(dbType.getEventId());
        RList<TicketType> cachedList = redissonClient.getList(cacheKey);

        if (cachedList.isExists() && !cachedList.isEmpty()) {
            List<TicketType> filtered = cachedList.readAll().stream()
                    .filter(t -> !t.getId().equals(ticketTypeId))
                    .toList();
            cachedList.clear();
            cachedList.addAll(filtered);
        }

        ticketTypeRepository.delete(dbType);
        return true;
    }

    @Override
    public List<TicketType> listTicketTypesForEvent(UUID eventId) {
        String cacheKey = getEventTicketTypesKey(eventId);
        RList<TicketType> cachedList = redissonClient.getList(cacheKey);

        if (cachedList.isExists() && !cachedList.isEmpty()) {
            return cachedList.readAll();
        }

        List<TicketType> ticketTypes = ticketTypeRepository.getTicketTypesByEventId(eventId);
        if (!ticketTypes.isEmpty()) {
            cachedList.addAll(ticketTypes);
        }

        return ticketTypes;
    }

    private String getEventTicketTypesKey(UUID eventId) {
        return GeneralConstants.REDIS_EVENT_PREFIX + eventId + GeneralConstants.REDIS_TICKET_TYPE_INFIX;
    }
}
