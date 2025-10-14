package com.ticketshall.events.mappers;

import com.ticketshall.events.dtos.params.CategoryParams;
import com.ticketshall.events.models.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryParams categoryParams);
//    CategoryResponse toCategoryResponse(Category category);
}
