package com.ticketshall.tickets.mapper;

import com.ticketshall.tickets.dto.CreateTicketTypeRequest;
import com.ticketshall.tickets.models.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketTypeMapper {
    @Mapping(source = "stock", target = "totalStock")
    @Mapping(source = "stock", target = "availableStock")
    TicketType toTicketType(CreateTicketTypeRequest request);
}
