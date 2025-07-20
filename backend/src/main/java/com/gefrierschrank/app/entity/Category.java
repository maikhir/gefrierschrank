package com.gefrierschrank.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name")
})
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 2, max = 50)
    @Column(nullable = false, unique = true)
    private String name;
    
    @Size(max = 100)
    @Column(name = "icon")
    private String icon;
    
    @NotBlank
    @Size(min = 1, max = 20)
    @Column(name = "default_unit", nullable = false)
    private String defaultUnit;
    
    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 5, fraction = 2)
    @Column(name = "unit_step", nullable = false)
    private BigDecimal unitStep = BigDecimal.ONE;
    
    @NotNull
    @DecimalMin(value = "0.0")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "min_value", nullable = false)
    private BigDecimal minValue = BigDecimal.ZERO;
    
    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "max_value", nullable = false)
    private BigDecimal maxValue = new BigDecimal("1000");
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public Category() {}
    
    public Category(String name, String defaultUnit, BigDecimal unitStep, 
                   BigDecimal minValue, BigDecimal maxValue) {
        this.name = name;
        this.defaultUnit = defaultUnit;
        this.unitStep = unitStep;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public Category(String name, String icon, String defaultUnit, BigDecimal unitStep, 
                   BigDecimal minValue, BigDecimal maxValue) {
        this(name, defaultUnit, unitStep, minValue, maxValue);
        this.icon = icon;
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
    
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", defaultUnit='" + defaultUnit + '\'' +
                ", unitStep=" + unitStep +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }
}