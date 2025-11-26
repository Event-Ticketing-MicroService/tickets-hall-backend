package com.ticketshall.events.controllers;

import com.ticketshall.events.dtos.params.CategoryParams;
import com.ticketshall.events.dtos.responses.ListResponse;
import com.ticketshall.events.models.Category;
import com.ticketshall.events.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CategoryController {

    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/admin")
    ResponseEntity<?> createCategory(@Valid @RequestBody CategoryParams categoryParams) {
        Category category = categoryService.createCategory(categoryParams);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/admin/{id}")
    ResponseEntity<?> updateCategory(@PathVariable UUID id, @Valid @RequestBody CategoryParams categoryParams) {
        Category category = categoryService.updateCategory(id, categoryParams);
        return ResponseEntity.ok(category);
    }

    @GetMapping("")
    ResponseEntity<ListResponse> getAllCategories(
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Sort sort,
            @RequestParam(required = false) String name) {
        List<Category> categories = categoryService.findAll(name, sort);
        ListResponse<Category> responseList = new ListResponse<>(categories, categories.size());
        return ResponseEntity.ok(responseList);
    }
}
