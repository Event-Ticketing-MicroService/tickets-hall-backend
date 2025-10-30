package com.ticketshall.events.mappers;

import com.ticketshall.events.dtos.params.CreateTicketTypeParams;
import com.ticketshall.events.models.TicketType;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketTypeMapper {
    List<TicketType> toTicketTypeList(List<CreateTicketTypeParams> ticketTypesParams);
}
