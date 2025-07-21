package com.gefrierschrank.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "CSV Item DTO for bulk import preview")
public class CsvItemDto {
    
    @Schema(description = "Row number in CSV file", example = "1")
    private int rowNumber;
    
    @Schema(description = "Item name", example = "Chicken Breast")
    private String name = "";
    
    @Schema(description = "Category name", example = "Fleisch")
    private String categoryName = "";
    
    @Schema(description = "Quantity", example = "1.5")
    private BigDecimal quantity;
    
    @Schema(description = "Unit", example = "kg")
    private String unit = "";
    
    @Schema(description = "Expiry date", example = "2024-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    @Schema(description = "Description", example = "Fresh chicken breast")
    private String description;
    
    @Schema(description = "Validation errors")
    private List<String> errors = new ArrayList<>();
    
    @Schema(description = "Whether this row is valid for import")
    private boolean valid = true;
    
    public CsvItemDto() {}
    
    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }
    
    public boolean isValid() {
        return valid && errors.isEmpty();
    }
    
    // Getters and setters
    public int getRowNumber() {
        return rowNumber;
    }
    
    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name != null ? name : "";
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName != null ? categoryName : "";
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
        this.unit = unit != null ? unit : "";
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors != null ? errors : new ArrayList<>();
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    @Override
    public String toString() {
        return "CsvItemDto{" +
                "rowNumber=" + rowNumber +
                ", name='" + name + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", expiryDate=" + expiryDate +
                ", valid=" + valid +
                ", errors=" + errors +
                '}';
    }
}