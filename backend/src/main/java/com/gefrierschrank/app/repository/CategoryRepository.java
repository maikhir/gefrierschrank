package com.gefrierschrank.app.repository;

import com.gefrierschrank.app.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    Optional<Category> findByNameIgnoreCase(String name);
    
    boolean existsByName(String name);
    
    boolean existsByNameIgnoreCase(String name);
    
    @Query("SELECT c FROM Category c ORDER BY c.name ASC")
    List<Category> findAllByOrderByNameAsc();
    
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Item i WHERE i.category.id = :categoryId")
    boolean hasItems(@Param("categoryId") Long categoryId);
    
    @Query("SELECT COUNT(c) FROM Category c")
    long countCategories();
}