package com.gefrierschrank.app.service;

import com.gefrierschrank.app.dto.CategoryDto;
import com.gefrierschrank.app.entity.Category;
import com.gefrierschrank.app.mapper.CategoryMapper;
import com.gefrierschrank.app.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        logger.debug("Fetching all categories");
        return categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        logger.debug("Fetching category with id: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }
    
    @Transactional(readOnly = true)
    public CategoryDto getCategoryByName(String name) {
        logger.debug("Fetching category with name: {}", name);
        Category category = categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with name: " + name));
        return categoryMapper.toDto(category);
    }
    
    public CategoryDto createCategory(CategoryDto categoryDto) {
        logger.info("Creating new category: {}", categoryDto.getName());
        
        // Check if category already exists
        if (categoryRepository.existsByNameIgnoreCase(categoryDto.getName())) {
            throw new DataIntegrityViolationException("Category with name '" + categoryDto.getName() + "' already exists");
        }
        
        // Validate unit step and min/max values
        if (categoryDto.getUnitStep().compareTo(categoryDto.getMaxValue()) > 0) {
            throw new IllegalArgumentException("Unit step cannot be greater than maximum value");
        }
        
        if (categoryDto.getMinValue().compareTo(categoryDto.getMaxValue()) > 0) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value");
        }
        
        Category category = categoryMapper.toEntity(categoryDto);
        category = categoryRepository.save(category);
        
        logger.info("Category created successfully with id: {}", category.getId());
        return categoryMapper.toDto(category);
    }
    
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        logger.info("Updating category with id: {}", id);
        
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        
        // Check if the new name conflicts with existing categories (excluding this one)
        if (!existingCategory.getName().equalsIgnoreCase(categoryDto.getName()) &&
            categoryRepository.existsByNameIgnoreCase(categoryDto.getName())) {
            throw new DataIntegrityViolationException("Category with name '" + categoryDto.getName() + "' already exists");
        }
        
        // Validate unit step and min/max values
        if (categoryDto.getUnitStep().compareTo(categoryDto.getMaxValue()) > 0) {
            throw new IllegalArgumentException("Unit step cannot be greater than maximum value");
        }
        
        if (categoryDto.getMinValue().compareTo(categoryDto.getMaxValue()) > 0) {
            throw new IllegalArgumentException("Minimum value cannot be greater than maximum value");
        }
        
        // Update fields using mapper
        categoryMapper.updateEntityFromDto(categoryDto, existingCategory);
        
        existingCategory = categoryRepository.save(existingCategory);
        
        logger.info("Category updated successfully with id: {}", existingCategory.getId());
        return categoryMapper.toDto(existingCategory);
    }
    
    public void deleteCategory(Long id) {
        logger.info("Deleting category with id: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        
        // Check if category has any items
        if (categoryRepository.hasItems(id)) {
            throw new DataIntegrityViolationException("Cannot delete category that contains items");
        }
        
        categoryRepository.delete(category);
        logger.info("Category deleted successfully with id: {}", id);
    }
    
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }
    
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }
}