package com.ticketshall.events.repositories.specifications;

import com.ticketshall.events.dtos.filterparams.EventFilterParams;
import com.ticketshall.events.models.Event;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EventSpecification {
    public static Specification<Event> hasName(String name) {
        return (root, query,  criteriaBuilder) -> {
            if(name == null || name.trim().isEmpty()) return criteriaBuilder.conjunction();
            String q = "%" + name.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), q);
        };
    }

    public static Specification<Event> hasLocation(String location) {
        return (root, query,  criteriaBuilder) -> {
            if(location == null || location.trim().isEmpty()) return criteriaBuilder.conjunction();
            String q = "%" + location.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("location")), q);
        };
    }


    public static Specification<Event> isPublished(Boolean isPublished) {
        return (root, query, criteriaBuilder) -> {
            if(isPublished == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get("isPublished"), isPublished);
        };
    }

    public static Specification<Event> inCategories(List<UUID> categoryIds) {
        return (root, query, criteriaBuilder) -> {
            if(categoryIds == null ||  categoryIds.isEmpty()) return criteriaBuilder.conjunction();
            return root.get("categoryId").in(categoryIds);
        };

    }

    public static Specification<Event> startsAfter(LocalDateTime startsAt) {
        return (root, query, criteriaBuilder) -> {
            if(startsAt == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.greaterThanOrEqualTo(root.get("startsAt"), startsAt);
        };
    }

    public static Specification<Event> endsBefore(LocalDateTime endsAt) {
        return (root, query, criteriaBuilder) -> {
            if(endsAt==null) return criteriaBuilder.conjunction();
            return criteriaBuilder.lessThanOrEqualTo(root.get("endsAt"), endsAt);
        };
    }

    public static Specification<Event> applyFilter(EventFilterParams eventFilterParams) {
        Specification<Event> spec = (root, query, builder) -> builder.conjunction();
        spec = spec.and(hasName(eventFilterParams.getName()));
        spec = spec.and(hasLocation(eventFilterParams.getLocation()));
        spec = spec.and(inCategories(eventFilterParams.getCategoryIds()));
        spec = spec.and(isPublished(eventFilterParams.getIsPublished()));
        spec = spec.and(startsAfter(eventFilterParams.getStartsAt()));
        spec = spec.and(endsBefore(eventFilterParams.getEndsAt()));
        return spec;
    }
}


