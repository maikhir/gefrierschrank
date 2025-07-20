package com.gefrierschrank.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gefrierschrank.app.dto.CategoryDto;
import com.gefrierschrank.app.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDto testCategoryDto;
    private CategoryDto createCategoryDto;

    @BeforeEach
    void setUp() {
        // Setup test CategoryDto
        testCategoryDto = new CategoryDto();
        testCategoryDto.setId(1L);
        testCategoryDto.setName("Fleisch");
        testCategoryDto.setIcon("meat");
        testCategoryDto.setDefaultUnit("kg");
        testCategoryDto.setUnitStep(new BigDecimal("0.1"));
        testCategoryDto.setMinValue(new BigDecimal("0.1"));
        testCategoryDto.setMaxValue(new BigDecimal("5.0"));

        // Setup create CategoryDto
        createCategoryDto = new CategoryDto();
        createCategoryDto.setName("Gemüse");
        createCategoryDto.setIcon("vegetable");
        createCategoryDto.setDefaultUnit("g");
        createCategoryDto.setUnitStep(new BigDecimal("50"));
        createCategoryDto.setMinValue(new BigDecimal("50"));
        createCategoryDto.setMaxValue(new BigDecimal("2000"));
    }

    @Test
    @WithMockUser
    void getAllCategories_ShouldReturnAllCategories() throws Exception {
        // Given
        List<CategoryDto> categories = Arrays.asList(testCategoryDto);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Fleisch"))
                .andExpect(jsonPath("$[0].icon").value("meat"));

        verify(categoryService).getAllCategories();
    }

    @Test
    @WithMockUser
    void getCategoryById_ValidId_ShouldReturnCategory() throws Exception {
        // Given
        when(categoryService.getCategoryById(1L)).thenReturn(testCategoryDto);

        // When & Then
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fleisch"))
                .andExpect(jsonPath("$.defaultUnit").value("kg"));

        verify(categoryService).getCategoryById(1L);
    }

    @Test
    @WithMockUser
    void getCategoryByName_ValidName_ShouldReturnCategory() throws Exception {
        // Given
        when(categoryService.getCategoryByName("Fleisch")).thenReturn(testCategoryDto);

        // When & Then
        mockMvc.perform(get("/api/categories/name/Fleisch"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fleisch"));

        verify(categoryService).getCategoryByName("Fleisch");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_AsAdmin_ShouldCreateCategory() throws Exception {
        // Given
        CategoryDto createdCategory = new CategoryDto();
        createdCategory.setId(2L);
        createdCategory.setName("Gemüse");
        createdCategory.setIcon("vegetable");
        createdCategory.setDefaultUnit("g");
        createdCategory.setUnitStep(new BigDecimal("50"));
        createdCategory.setMinValue(new BigDecimal("50"));
        createdCategory.setMaxValue(new BigDecimal("2000"));

        when(categoryService.createCategory(any(CategoryDto.class))).thenReturn(createdCategory);

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Gemüse"));

        verify(categoryService).createCategory(any(CategoryDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategory_AsUser_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryDto)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_InvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        createCategoryDto.setName(null); // Invalid - name is required

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_AsAdmin_ShouldUpdateCategory() throws Exception {
        // Given
        CategoryDto updatedCategory = new CategoryDto();
        updatedCategory.setId(1L);
        updatedCategory.setName("Updated Fleisch");
        updatedCategory.setIcon("updated-meat");
        updatedCategory.setDefaultUnit("kg");
        updatedCategory.setUnitStep(new BigDecimal("0.2"));
        updatedCategory.setMinValue(new BigDecimal("0.2"));
        updatedCategory.setMaxValue(new BigDecimal("10.0"));

        when(categoryService.updateCategory(eq(1L), any(CategoryDto.class))).thenReturn(updatedCategory);

        // When & Then
        mockMvc.perform(put("/api/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Fleisch"));

        verify(categoryService).updateCategory(eq(1L), any(CategoryDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCategory_AsUser_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryDto)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).updateCategory(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_AsAdmin_ShouldDeleteCategory() throws Exception {
        // Given
        doNothing().when(categoryService).deleteCategory(1L);

        // When & Then
        mockMvc.perform(delete("/api/categories/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCategory_AsUser_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/categories/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).deleteCategory(any());
    }

    @Test
    @WithMockUser
    void existsById_ExistingId_ShouldReturnTrue() throws Exception {
        // Given
        when(categoryService.existsById(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/categories/1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));

        verify(categoryService).existsById(1L);
    }

    @Test
    @WithMockUser
    void existsById_NonExistingId_ShouldReturnFalse() throws Exception {
        // Given
        when(categoryService.existsById(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/categories/999/exists"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("false"));

        verify(categoryService).existsById(999L);
    }

    @Test
    @WithMockUser
    void existsByName_ExistingName_ShouldReturnTrue() throws Exception {
        // Given
        when(categoryService.existsByName("Fleisch")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/categories/name/Fleisch/exists"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));

        verify(categoryService).existsByName("Fleisch");
    }

    @Test
    @WithMockUser
    void existsByName_NonExistingName_ShouldReturnFalse() throws Exception {
        // Given
        when(categoryService.existsByName("NonExistent")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/categories/name/NonExistent/exists"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("false"));

        verify(categoryService).existsByName("NonExistent");
    }

    @Test
    void getAllCategories_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).getAllCategories();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_WithoutCsrf_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryDto)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_EmptyName_ShouldReturnBadRequest() throws Exception {
        // Given
        createCategoryDto.setName("");

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_InvalidMinMaxValues_ShouldReturnBadRequest() throws Exception {
        // Given
        createCategoryDto.setMinValue(new BigDecimal("100"));
        createCategoryDto.setMaxValue(new BigDecimal("50")); // max < min

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCategoryDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any());
    }
}