package com.ticketshall.events.repositories;

import com.ticketshall.events.dtos.responses.CategoryResponse;
import com.ticketshall.events.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    @Query(value = "SELECT EXISTS(SELECT 1 FROM categories WHERE name = :name)", nativeQuery = true)
    boolean existsByNameNative(@Param("name") String name);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM categories WHERE name = :name AND id <> :id)", nativeQuery = true)
    boolean existsAnotherOneByNameNative(@Param("name") String name, @Param("id") UUID id);

    @Query("SELECT new com.ticketshall.events.dtos.responses.CategoryResponse(c.id, c.name) FROM Category c")
    List<CategoryResponse> findAllCategories();
}
