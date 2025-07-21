package com.gefrierschrank.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "CSV Import Request containing items to import")
public class CsvImportRequest {
    
    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    @Schema(description = "List of items to import from CSV", required = true)
    private List<CsvItemDto> items;
    
    @Schema(description = "Skip items with validation errors", example = "true")
    private boolean skipInvalidItems = true;
    
    public CsvImportRequest() {}
    
    public CsvImportRequest(List<CsvItemDto> items, boolean skipInvalidItems) {
        this.items = items;
        this.skipInvalidItems = skipInvalidItems;
    }
    
    // Getters and setters
    public List<CsvItemDto> getItems() {
        return items;
    }
    
    public void setItems(List<CsvItemDto> items) {
        this.items = items;
    }
    
    public boolean isSkipInvalidItems() {
        return skipInvalidItems;
    }
    
    public void setSkipInvalidItems(boolean skipInvalidItems) {
        this.skipInvalidItems = skipInvalidItems;
    }
    
    @Override
    public String toString() {
        return "CsvImportRequest{" +
                "items=" + (items != null ? items.size() : 0) + " items" +
                ", skipInvalidItems=" + skipInvalidItems +
                '}';
    }
}