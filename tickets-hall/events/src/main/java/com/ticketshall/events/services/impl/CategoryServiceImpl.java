package com.ticketshall.events.services.impl;

import com.ticketshall.events.dtos.params.CategoryParams;
import com.ticketshall.events.exceptions.ConflictErrorException;
import com.ticketshall.events.exceptions.NotFoundException;
import com.ticketshall.events.mappers.CategoryMapper;
import com.ticketshall.events.models.Category;
import com.ticketshall.events.repositories.CategoryRepository;
import com.ticketshall.events.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    @Autowired
    CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }



    @Override
    public Category createCategory(CategoryParams categoryParams) {
        categoryParams.setName(categoryParams.getName().toLowerCase());
        if(categoryRepository.existsByNameNative(categoryParams.getName())) throw new ConflictErrorException("Category with this name already exists");
        return categoryRepository.save(categoryMapper.toCategory(categoryParams));
    }

    @Override
    public Category updateCategory(UUID categoryId, CategoryParams categoryParams) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
        if(categoryOptional.isEmpty()) throw new NotFoundException("category with this id doesn't exist");

        categoryParams.setName(categoryParams.getName().toLowerCase()); // lowercase it
        if(categoryRepository.existsAnotherOneByNameNative(categoryParams.getName(), categoryId)) throw new ConflictErrorException("Category with this name already exists");

        Category category = categoryOptional.get();
        category.setName(categoryParams.getName());

        return categoryRepository.save(category);
    }

    @Override
    public List<Category> findAll(String name, Sort sort) {
        List<Category> categories;
        if(name != null) {
            categories = categoryRepository.findByNameContainingIgnoreCase(name, sort);
        } else {
            categories = categoryRepository.findAll(sort);
        }
        return categories;
    }
}
