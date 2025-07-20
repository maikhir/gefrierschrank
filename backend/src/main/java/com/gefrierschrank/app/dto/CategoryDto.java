package com.gefrierschrank.app.dto;

import com.gefrierschrank.app.entity.Category;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CategoryDto {
    
    private Long id;
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    private String name;
    
    @Size(max = 100, message = "Icon path must not exceed 100 characters")
    private String icon;
    
    @NotBlank(message = "Default unit is required")
    @Size(min = 1, max = 20, message = "Default unit must be between 1 and 20 characters")
    private String defaultUnit;
    
    @NotNull(message = "Unit step is required")
    @DecimalMin(value = "0.01", message = "Unit step must be at least 0.01")
    @Digits(integer = 5, fraction = 2, message = "Unit step must have at most 5 integer digits and 2 decimal places")
    private BigDecimal unitStep;
    
    @NotNull(message = "Minimum value is required")
    @DecimalMin(value = "0.0", message = "Minimum value must be at least 0")
    @Digits(integer = 8, fraction = 2, message = "Minimum value must have at most 8 integer digits and 2 decimal places")
    private BigDecimal minValue;
    
    @NotNull(message = "Maximum value is required")
    @DecimalMin(value = "0.01", message = "Maximum value must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Maximum value must have at most 8 integer digits and 2 decimal places")
    private BigDecimal maxValue;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public CategoryDto() {}
    
    public CategoryDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.icon = category.getIcon();
        this.defaultUnit = category.getDefaultUnit();
        this.unitStep = category.getUnitStep();
        this.minValue = category.getMinValue();
        this.maxValue = category.getMaxValue();
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
    }
    
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getDefaultUnit() {
        return defaultUnit;
    }
    
    public void setDefaultUnit(String defaultUnit) {
        this.defaultUnit = defaultUnit;
    }
    
    public BigDecimal getUnitStep() {
        return unitStep;
    }
    
    public void setUnitStep(BigDecimal unitStep) {
        this.unitStep = unitStep;
    }
    
    public BigDecimal getMinValue() {
        return minValue;
    }
    
    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }
    
    public BigDecimal getMaxValue() {
        return maxValue;
    }
    
    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}