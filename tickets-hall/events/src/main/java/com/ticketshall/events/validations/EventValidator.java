package com.ticketshall.events.validations;

import com.ticketshall.events.dtos.params.CreateEventParams;
import com.ticketshall.events.exceptions.BadRequestException;

public class EventValidator implements Validator{
    CreateEventParams createEventParams;

    public EventValidator(CreateEventParams createEventParams) {
        this.createEventParams = createEventParams;
    }

    private boolean hasValidEndDate() {
        return createEventParams.getEndsAt().isAfter(createEventParams.getStartsAt())
                && !createEventParams.getEndsAt().equals(createEventParams.getStartsAt());
    }

    @Override
    public void validate() {
        if(!hasValidEndDate()) {
            throw new BadRequestException("endsAt must be after startsAt");
        }
    }
}
