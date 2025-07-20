package com.gefrierschrank.app.dto;

import com.gefrierschrank.app.constants.AppConstants;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class ItemFilterRequest {
    
    @Parameter(description = "Category ID filter")
    private Long categoryId;
    
    @Parameter(description = "Search term for item name")
    private String searchTerm;
    
    @Parameter(description = "Filter expiring soon items")
    private Boolean expiringSoon;
    
    @Parameter(description = "Days for expiry filter")
    @Min(value = AppConstants.MIN_EXPIRY_DAYS, message = "Expiry days must be at least 1")
    @Max(value = AppConstants.MAX_EXPIRY_DAYS, message = "Expiry days cannot exceed 365")
    private Integer expiryDays = AppConstants.DEFAULT_EXPIRY_WARNING_DAYS;
    
    @Parameter(description = "Sort by field (name, expiry, category, quantity, created)")
    private String sortBy = "created";
    
    @Parameter(description = "Page number (0-based)")
    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page = AppConstants.DEFAULT_PAGE_NUMBER;
    
    @Parameter(description = "Page size")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = AppConstants.MAX_PAGE_SIZE, message = "Page size cannot exceed 100")
    private Integer size = AppConstants.DEFAULT_PAGE_SIZE;

    public ItemFilterRequest() {}

    // Getters and setters
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Boolean getExpiringSoon() {
        return expiringSoon;
    }

    public void setExpiringSoon(Boolean expiringSoon) {
        this.expiringSoon = expiringSoon;
    }

    public Integer getExpiryDays() {
        return expiryDays;
    }

    public void setExpiryDays(Integer expiryDays) {
        this.expiryDays = expiryDays;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "ItemFilterRequest{" +
                "categoryId=" + categoryId +
                ", searchTerm='" + searchTerm + '\'' +
                ", expiringSoon=" + expiringSoon +
                ", expiryDays=" + expiryDays +
                ", sortBy='" + sortBy + '\'' +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}