package com.gefrierschrank.app.dto;

import com.gefrierschrank.app.entity.ExpiryType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateItemRequest {
    
    @NotBlank(message = "Item name is required")
    @Size(min = 1, max = 100, message = "Item name must be between 1 and 100 characters")
    private String name;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Quantity must have at most 8 integer digits and 2 decimal places")
    private BigDecimal quantity;
    
    @NotBlank(message = "Unit is required")
    @Size(min = 1, max = 20, message = "Unit must be between 1 and 20 characters")
    private String unit;
    
    private LocalDate expiryDate;
    
    private ExpiryType expiryType = ExpiryType.BEST_BEFORE;
    
    @Size(max = 500, message = "Photo path must not exceed 500 characters")
    private String photoPath;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    public CreateItemRequest() {}
    
    public CreateItemRequest(String name, Long categoryId, BigDecimal quantity, String unit, 
                           LocalDate expiryDate, ExpiryType expiryType) {
        this.name = name;
        this.categoryId = categoryId;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.expiryType = expiryType != null ? expiryType : ExpiryType.BEST_BEFORE;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public ExpiryType getExpiryType() {
        return expiryType;
    }
    
    public void setExpiryType(ExpiryType expiryType) {
        this.expiryType = expiryType;
    }
    
    public String getPhotoPath() {
        return photoPath;
    }
    
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}