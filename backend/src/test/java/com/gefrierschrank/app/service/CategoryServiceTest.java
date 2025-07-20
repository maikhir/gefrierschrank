package com.gefrierschrank.app.service;

import com.gefrierschrank.app.dto.CategoryDto;
import com.gefrierschrank.app.entity.Category;
import com.gefrierschrank.app.mapper.CategoryMapper;
import com.gefrierschrank.app.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryMapper categoryMapper = new CategoryMapper();

    private CategoryService categoryService;

    private Category testCategory;
    private CategoryDto testCategoryDto;

    @BeforeEach
    void setUp() {
        // Setup test category entity
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Fleisch");
        testCategory.setIcon("meat");
        testCategory.setDefaultUnit("kg");
        testCategory.setUnitStep(new BigDecimal("0.1"));
        testCategory.setMinValue(new BigDecimal("0.1"));
        testCategory.setMaxValue(new BigDecimal("5.0"));

        // Setup test category DTO
        testCategoryDto = new CategoryDto();
        testCategoryDto.setId(1L);
        testCategoryDto.setName("Fleisch");
        testCategoryDto.setIcon("meat");
        testCategoryDto.setDefaultUnit("kg");
        testCategoryDto.setUnitStep(new BigDecimal("0.1"));
        testCategoryDto.setMinValue(new BigDecimal("0.1"));
        testCategoryDto.setMaxValue(new BigDecimal("5.0"));

        // Initialize CategoryService with mocked repository and real mapper
        categoryService = new CategoryService(categoryRepository, categoryMapper);
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Given
        List<Category> categories = Arrays.asList(testCategory);
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(categories);

        // When
        List<CategoryDto> result = categoryService.getAllCategories();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fleisch");
        assertThat(result.get(0).getIcon()).isEqualTo("meat");
        verify(categoryRepository).findAllByOrderByNameAsc();
    }

    @Test
    void getAllCategories_EmptyList_ShouldReturnEmptyList() {
        // Given
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(Arrays.asList());

        // When
        List<CategoryDto> result = categoryService.getAllCategories();

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository).findAllByOrderByNameAsc();
    }

    @Test
    void getCategoryById_ValidId_ShouldReturnCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When
        CategoryDto result = categoryService.getCategoryById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Fleisch");
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_InvalidId_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found with id: 999");
    }

    @Test
    void getCategoryByName_ValidName_ShouldReturnCategory() {
        // Given
        when(categoryRepository.findByNameIgnoreCase("Fleisch")).thenReturn(Optional.of(testCategory));

        // When
        CategoryDto result = categoryService.getCategoryByName("Fleisch");

        // Then
        assertThat(result.getName()).isEqualTo("Fleisch");
        verify(categoryRepository).findByNameIgnoreCase("Fleisch");
    }

    @Test
    void getCategoryByName_InvalidName_ShouldThrowException() {
        // Given
        when(categoryRepository.findByNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryByName("NonExistent"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found with name: NonExistent");
    }

    @Test
    void createCategory_ValidCategory_ShouldCreateCategory() {
        // Given
        CategoryDto newCategoryDto = new CategoryDto();
        newCategoryDto.setName("Gemüse");
        newCategoryDto.setIcon("vegetable");
        newCategoryDto.setDefaultUnit("g");
        newCategoryDto.setUnitStep(new BigDecimal("50"));
        newCategoryDto.setMinValue(new BigDecimal("50"));
        newCategoryDto.setMaxValue(new BigDecimal("2000"));

        Category savedCategory = new Category();
        savedCategory.setId(2L);
        savedCategory.setName("Gemüse");
        savedCategory.setIcon("vegetable");
        savedCategory.setDefaultUnit("g");
        savedCategory.setUnitStep(new BigDecimal("50"));
        savedCategory.setMinValue(new BigDecimal("50"));
        savedCategory.setMaxValue(new BigDecimal("2000"));

        when(categoryRepository.existsByNameIgnoreCase("Gemüse")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // When
        CategoryDto result = categoryService.createCategory(newCategoryDto);

        // Then
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Gemüse");
        verify(categoryRepository).existsByNameIgnoreCase("Gemüse");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName_ShouldThrowException() {
        // Given
        when(categoryRepository.existsByNameIgnoreCase("Fleisch")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(testCategoryDto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Category with name 'Fleisch' already exists");
    }

    @Test
    void createCategory_UnitStepGreaterThanMaxValue_ShouldThrowException() {
        // Given
        testCategoryDto.setUnitStep(new BigDecimal("10.0"));
        testCategoryDto.setMaxValue(new BigDecimal("5.0"));
        when(categoryRepository.existsByNameIgnoreCase("Fleisch")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(testCategoryDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unit step cannot be greater than maximum value");
    }

    @Test
    void createCategory_MinValueGreaterThanMaxValue_ShouldThrowException() {
        // Given
        testCategoryDto.setMinValue(new BigDecimal("10.0"));
        testCategoryDto.setMaxValue(new BigDecimal("5.0"));
        when(categoryRepository.existsByNameIgnoreCase("Fleisch")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(testCategoryDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Minimum value cannot be greater than maximum value");
    }

    @Test
    void updateCategory_ValidUpdate_ShouldUpdateCategory() {
        // Given
        CategoryDto updatedDto = new CategoryDto();
        updatedDto.setName("Updated Fleisch");
        updatedDto.setIcon("updated-meat");
        updatedDto.setDefaultUnit("kg");
        updatedDto.setUnitStep(new BigDecimal("0.2"));
        updatedDto.setMinValue(new BigDecimal("0.2"));
        updatedDto.setMaxValue(new BigDecimal("10.0"));

        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Updated Fleisch");
        updatedCategory.setIcon("updated-meat");
        updatedCategory.setDefaultUnit("kg");
        updatedCategory.setUnitStep(new BigDecimal("0.2"));
        updatedCategory.setMinValue(new BigDecimal("0.2"));
        updatedCategory.setMaxValue(new BigDecimal("10.0"));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameIgnoreCase("Updated Fleisch")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // When
        CategoryDto result = categoryService.updateCategory(1L, updatedDto);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Fleisch");
        assertThat(result.getIcon()).isEqualTo("updated-meat");
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void updateCategory_CategoryNotFound_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(999L, testCategoryDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found with id: 999");
    }

    @Test
    void updateCategory_DuplicateNameWithDifferentCategory_ShouldThrowException() {
        // Given
        testCategoryDto.setName("ExistingCategory");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByNameIgnoreCase("ExistingCategory")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(1L, testCategoryDto))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Category with name 'ExistingCategory' already exists");
    }

    @Test
    void updateCategory_SameName_ShouldAllowUpdate() {
        // Given
        testCategoryDto.setName("Fleisch"); // Same name as existing
        testCategoryDto.setIcon("updated-icon");
        
        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Fleisch");
        updatedCategory.setIcon("updated-icon");
        updatedCategory.setDefaultUnit("kg");
        updatedCategory.setUnitStep(new BigDecimal("0.1"));
        updatedCategory.setMinValue(new BigDecimal("0.1"));
        updatedCategory.setMaxValue(new BigDecimal("5.0"));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // When
        CategoryDto result = categoryService.updateCategory(1L, testCategoryDto);

        // Then
        assertThat(result.getName()).isEqualTo("Fleisch");
        assertThat(result.getIcon()).isEqualTo("updated-icon");
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void deleteCategory_ValidId_ShouldDeleteCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.hasItems(1L)).thenReturn(false);

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).hasItems(1L);
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void deleteCategory_CategoryNotFound_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found with id: 999");
    }

    @Test
    void deleteCategory_CategoryHasItems_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.hasItems(1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Cannot delete category that contains items");
    }

    @Test
    void existsById_ExistingId_ShouldReturnTrue() {
        // Given
        when(categoryRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = categoryService.existsById(1L);

        // Then
        assertThat(result).isTrue();
        verify(categoryRepository).existsById(1L);
    }

    @Test
    void existsById_NonExistingId_ShouldReturnFalse() {
        // Given
        when(categoryRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = categoryService.existsById(999L);

        // Then
        assertThat(result).isFalse();
        verify(categoryRepository).existsById(999L);
    }

    @Test
    void existsByName_ExistingName_ShouldReturnTrue() {
        // Given
        when(categoryRepository.existsByNameIgnoreCase("Fleisch")).thenReturn(true);

        // When
        boolean result = categoryService.existsByName("Fleisch");

        // Then
        assertThat(result).isTrue();
        verify(categoryRepository).existsByNameIgnoreCase("Fleisch");
    }

    @Test
    void existsByName_NonExistingName_ShouldReturnFalse() {
        // Given
        when(categoryRepository.existsByNameIgnoreCase("NonExistent")).thenReturn(false);

        // When
        boolean result = categoryService.existsByName("NonExistent");

        // Then
        assertThat(result).isFalse();
        verify(categoryRepository).existsByNameIgnoreCase("NonExistent");
    }
}