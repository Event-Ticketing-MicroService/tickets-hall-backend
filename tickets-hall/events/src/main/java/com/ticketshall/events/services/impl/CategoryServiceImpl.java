package com.ticketshall.events.services.impl;

import com.ticketshall.events.dtos.params.CategoryParams;
import com.ticketshall.events.exceptions.ConflictErrorException;
import com.ticketshall.events.mappers.CategoryMapper;
import com.ticketshall.events.repositories.CategoryRepository;
import com.ticketshall.events.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void save(CategoryParams categoryParams) {
        categoryParams.setName(categoryParams.getName().toLowerCase());
        if(categoryRepository.existsByNameNative(categoryParams.getName())) throw new ConflictErrorException("Category with this name already exists");
        categoryRepository.save(categoryMapper.toCategory(categoryParams));
    }
}
