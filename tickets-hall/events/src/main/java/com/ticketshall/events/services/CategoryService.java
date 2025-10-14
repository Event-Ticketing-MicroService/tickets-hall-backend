package com.ticketshall.events.services;

import com.ticketshall.events.dtos.params.CategoryParams;
import com.ticketshall.events.dtos.responses.CategoryResponse;
import com.ticketshall.events.models.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    Category createCategory(CategoryParams categoryParams);
    Category updateCategory(UUID categoryId, CategoryParams categoryParams);
    List<CategoryResponse> findAll();
}
