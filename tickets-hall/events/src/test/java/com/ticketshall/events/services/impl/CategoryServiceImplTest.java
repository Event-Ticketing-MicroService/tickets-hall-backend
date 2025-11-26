package com.ticketshall.events.services.impl;

import com.ticketshall.events.dtos.params.CategoryParams;
import com.ticketshall.events.exceptions.ConflictErrorException;
import com.ticketshall.events.exceptions.NotFoundException;
import com.ticketshall.events.mappers.CategoryMapper;
import com.ticketshall.events.models.Category;
import com.ticketshall.events.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryParams categoryParams;
    private Category category;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        categoryParams = CategoryParams.builder().name("Music").build();

        category = new Category();
        category.setId(categoryId);
        category.setName("music");
    }

    @Test
    void createCategory_ShouldSaveAndReturnCategory() {
        // Arrange
        when(categoryRepository.existsByNameNative("music")).thenReturn(false);
        when(categoryMapper.toCategory(categoryParams)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);

        // Act
        Category result = categoryService.createCategory(categoryParams);

        // Assert
        assertNotNull(result);
        assertEquals("music", result.getName());
        verify(categoryRepository).existsByNameNative("music");
        verify(categoryRepository).save(category);
    }

    @Test
    void createCategory_WhenNameExists_ShouldThrowConflictException() {
        // Arrange
        when(categoryRepository.existsByNameNative("music")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictErrorException.class, () -> categoryService.createCategory(categoryParams));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_ShouldUpdateAndReturnCategory() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsAnotherOneByNameNative("music", categoryId)).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);

        // Act
        Category result = categoryService.updateCategory(categoryId, categoryParams);

        // Assert
        assertNotNull(result);
        assertEquals("music", result.getName());
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_WhenNotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> categoryService.updateCategory(categoryId, categoryParams));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_WhenNameExistsForAnother_ShouldThrowConflictException() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsAnotherOneByNameNative("music", categoryId)).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictErrorException.class, () -> categoryService.updateCategory(categoryId, categoryParams));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void findAll_ShouldReturnCategories() {
        // Arrange
        Sort sort = Sort.by("name");
        when(categoryRepository.findAll(sort)).thenReturn(List.of(category));

        // Act
        List<Category> result = categoryService.findAll(null, sort);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(categoryRepository).findAll(sort);
    }

    @Test
    void findAll_WithSearch_ShouldReturnFilteredCategories() {
        // Arrange
        Sort sort = Sort.by("name");
        when(categoryRepository.findByNameContainingIgnoreCase("mu", sort)).thenReturn(List.of(category));

        // Act
        List<Category> result = categoryService.findAll("mu", sort);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(categoryRepository).findByNameContainingIgnoreCase("mu", sort);
    }
}
