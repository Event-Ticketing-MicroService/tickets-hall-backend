package com.ticketshall.events.controllers;

import com.ticketshall.events.dtos.params.CategoryParams;
import com.ticketshall.events.dtos.responses.CategoryResponse;
import com.ticketshall.events.dtos.responses.ListResponse;
import com.ticketshall.events.models.Category;
import com.ticketshall.events.services.CategoryService;
import jakarta.validation.Valid;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }


    @PostMapping("")
    ResponseEntity<?> createCategory(@Valid @RequestBody CategoryParams categoryParams) {
        Category category = categoryService.createCategory(categoryParams);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/{id}")
    ResponseEntity<?> updateCategory(@PathVariable UUID id, @Valid @RequestBody CategoryParams categoryParams) {
        Category category = categoryService.updateCategory(id, categoryParams);
        return ResponseEntity.ok(category);
    }

    @GetMapping("")
    ResponseEntity<ListResponse> getAllCategories() {
        List<CategoryResponse> categories = categoryService.findAll();
        ListResponse<CategoryResponse> responseList = new ListResponse<>(categories, categories.size());
        return ResponseEntity.ok(responseList);
    }
}
