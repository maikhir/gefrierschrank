package com.gefrierschrank.app.dto;

import com.gefrierschrank.app.entity.ExpiryType;
import com.gefrierschrank.app.entity.Item;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItemDto {
    
    private Long id;
    
    @NotBlank(message = "Item name is required")
    @Size(min = 1, max = 100, message = "Item name must be between 1 and 100 characters")
    private String name;
    
    @NotNull(message = "Category is required")
    private Long categoryId;
    
    private String categoryName;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Quantity must have at most 8 integer digits and 2 decimal places")
    private BigDecimal quantity;
    
    @NotBlank(message = "Unit is required")
    @Size(min = 1, max = 20, message = "Unit must be between 1 and 20 characters")
    private String unit;
    
    private LocalDate expiryDate;
    
    @NotNull(message = "Expiry type is required")
    private ExpiryType expiryType = ExpiryType.BEST_BEFORE;
    
    @Size(max = 500, message = "Photo path must not exceed 500 characters")
    private String photoPath;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private Long userId;
    private String username;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private boolean expiringSoon;
    private boolean expired;
    private long daysUntilExpiry;
    
    public ItemDto() {}
    
    public ItemDto(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.categoryId = item.getCategory().getId();
        this.categoryName = item.getCategory().getName();
        this.quantity = item.getQuantity();
        this.unit = item.getUnit();
        this.expiryDate = item.getExpiryDate();
        this.expiryType = item.getExpiryType();
        this.photoPath = item.getPhotoPath();
        this.description = item.getDescription();
        this.userId = item.getUser().getId();
        this.username = item.getUser().getUsername();
        this.createdAt = item.getCreatedAt();
        this.updatedAt = item.getUpdatedAt();
        
        // Compute expiry information
        this.expiringSoon = item.isExpiringSoon(7);
        this.expired = item.isExpired();
        this.daysUntilExpiry = item.getDaysUntilExpiry();
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
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
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
    
    public boolean isExpiringSoon() {
        return expiringSoon;
    }
    
    public void setExpiringSoon(boolean expiringSoon) {
        this.expiringSoon = expiringSoon;
    }
    
    public boolean isExpired() {
        return expired;
    }
    
    public void setExpired(boolean expired) {
        this.expired = expired;
    }
    
    public long getDaysUntilExpiry() {
        return daysUntilExpiry;
    }
    
    public void setDaysUntilExpiry(long daysUntilExpiry) {
        this.daysUntilExpiry = daysUntilExpiry;
    }
}