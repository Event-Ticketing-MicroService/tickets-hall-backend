package com.ticketshall.events.dtos.filterparams;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventFilterParams {
    private String name;
    private String location;
    private List<UUID> categoryIds;
    private Boolean isPublished;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
}
