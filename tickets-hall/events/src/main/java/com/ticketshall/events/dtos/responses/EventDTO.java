package com.ticketshall.events.dtos.responses;

import com.ticketshall.events.models.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {
    private UUID id;

    private CategoryDTO category;

    private String name;

    private String description;

    private String location;

    private Double latitude;

    private Double longitude;

    private String backgroundImageUrl;

    private Boolean isPublished = false;

    private LocalDateTime publishedAt;

    private LocalDateTime startsAt;

    private LocalDateTime endsAt;

    private Integer totalAvailableTickets;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
