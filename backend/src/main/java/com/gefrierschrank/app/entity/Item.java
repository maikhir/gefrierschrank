package com.gefrierschrank.app.entity;

import com.gefrierschrank.app.constants.AppConstants;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "items", indexes = {
    @Index(name = "idx_item_user_id", columnList = "user_id"),
    @Index(name = "idx_item_category_id", columnList = "category_id"),
    @Index(name = "idx_item_expiry_date", columnList = "expiry_date"),
    @Index(name = "idx_item_created_at", columnList = "created_at")
})
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 1, max = 100)
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull
    private Category category;
    
    @NotNull
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @Digits(integer = 8, fraction = 2)
    @Column(nullable = false)
    private BigDecimal quantity;
    
    @NotBlank
    @Size(min = 1, max = 20)
    @Column(nullable = false)
    private String unit;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "expiry_type", nullable = false)
    private ExpiryType expiryType = ExpiryType.BEST_BEFORE;
    
    @Size(max = 500)
    @Column(name = "photo_path")
    private String photoPath;
    
    @Size(max = 500)
    @Column(name = "description")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public Item() {}
    
    public Item(String name, Category category, BigDecimal quantity, String unit, 
               LocalDate expiryDate, ExpiryType expiryType, User user) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.expiryType = expiryType != null ? expiryType : ExpiryType.BEST_BEFORE;
        this.user = user;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean isExpiringSoon(int days) {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDate.now().plusDays(days + AppConstants.EXPIRY_BUFFER_DAYS));
    }
    
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDate.now());
    }
    
    public long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return Long.MAX_VALUE;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
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
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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
    
    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", expiryDate=" + expiryDate +
                ", expiryType=" + expiryType +
                '}';
    }
}