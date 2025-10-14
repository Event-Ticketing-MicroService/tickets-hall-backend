package com.ticketshall.events.services;

import com.ticketshall.events.dtos.params.CategoryParams;
import com.ticketshall.events.models.Category;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    Category createCategory(CategoryParams categoryParams);
    Category updateCategory(UUID categoryId, CategoryParams categoryParams);
    List<Category> findAll(String name, Sort sort);
}
