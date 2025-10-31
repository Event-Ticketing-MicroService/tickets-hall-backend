package com.ticketshall.events.validations;

import com.ticketshall.events.dtos.params.UpsertEventParams;
import com.ticketshall.events.exceptions.BadRequestException;

public class EventValidator implements Validator{
    UpsertEventParams UpsertEventParams;

    public EventValidator(UpsertEventParams UpsertEventParams) {
        this.UpsertEventParams = UpsertEventParams;
    }

    private boolean hasValidEndDate() {
        return UpsertEventParams.getEndsAt().isAfter(UpsertEventParams.getStartsAt())
                && !UpsertEventParams.getEndsAt().equals(UpsertEventParams.getStartsAt());
    }

    @Override
    public void validate() {
        if(!hasValidEndDate()) {
            throw new BadRequestException("endsAt must be after startsAt");
        }
    }
}
