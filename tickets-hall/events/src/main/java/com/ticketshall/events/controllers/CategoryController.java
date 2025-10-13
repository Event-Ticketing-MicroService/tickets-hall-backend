package com.ticketshall.events.controllers;

import com.ticketshall.events.dtos.params.CategoryParams;
import com.ticketshall.events.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        categoryService.save(categoryParams);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
