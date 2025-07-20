package com.gefrierschrank.app.controller;

import com.gefrierschrank.app.constants.AppConstants;
import com.gefrierschrank.app.dto.CreateItemRequest;
import com.gefrierschrank.app.dto.ItemDto;
import com.gefrierschrank.app.dto.ItemFilterRequest;
import com.gefrierschrank.app.dto.UpdateItemRequest;
import com.gefrierschrank.app.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@Tag(name = "Item Management", description = "APIs for managing freezer items")
public class ItemController {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    
    private final ItemService itemService;
    
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }
    
    @GetMapping
    @Operation(summary = "Get all items for user", description = "Retrieve all items for the authenticated user")
    public ResponseEntity<List<ItemDto>> getAllItems(Authentication authentication) {
        logger.info("GET /api/items - Fetching all items for user: {}", authentication.getName());
        List<ItemDto> items = itemService.getAllItemsByUser(authentication.getName());
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/paginated")
    @Operation(summary = "Get paginated items", description = "Retrieve paginated items for the authenticated user")
    public ResponseEntity<Page<ItemDto>> getItemsPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size,
            Authentication authentication) {
        
        logger.info("GET /api/items/paginated - Fetching paginated items for user: {}", authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        Page<ItemDto> items = itemService.getItemsByUserPaginated(authentication.getName(), pageable);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieve a specific item by its ID")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Long id, Authentication authentication) {
        logger.info("GET /api/items/{} - Fetching item for user: {}", id, authentication.getName());
        ItemDto item = itemService.getItemById(id, authentication.getName());
        return ResponseEntity.ok(item);
    }
    
    @PostMapping
    @Operation(summary = "Create new item", description = "Create a new freezer item")
    public ResponseEntity<ItemDto> createItem(@Valid @RequestBody CreateItemRequest request, 
                                             Authentication authentication) {
        logger.info("POST /api/items - Creating item: {} for user: {}", request.getName(), authentication.getName());
        ItemDto createdItem = itemService.createItem(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update item", description = "Update an existing freezer item")
    public ResponseEntity<ItemDto> updateItem(@PathVariable Long id,
                                             @Valid @RequestBody UpdateItemRequest request,
                                             Authentication authentication) {
        logger.info("PUT /api/items/{} - Updating item for user: {}", id, authentication.getName());
        ItemDto updatedItem = itemService.updateItem(id, request, authentication.getName());
        return ResponseEntity.ok(updatedItem);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item", description = "Delete a freezer item")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id, Authentication authentication) {
        logger.info("DELETE /api/items/{} - Deleting item for user: {}", id, authentication.getName());
        itemService.deleteItem(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
    
    // Search and filter endpoints
    @GetMapping("/search")
    @Operation(summary = "Search items by name", description = "Search items by name containing the search term")
    public ResponseEntity<List<ItemDto>> searchItemsByName(
            @Parameter(description = "Search term") @RequestParam String q,
            Authentication authentication) {
        
        logger.info("GET /api/items/search?q={} - Searching items for user: {}", q, authentication.getName());
        List<ItemDto> items = itemService.searchItemsByName(q, authentication.getName());
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get items by category", description = "Retrieve all items for a specific category")
    public ResponseEntity<List<ItemDto>> getItemsByCategory(@PathVariable Long categoryId,
                                                           Authentication authentication) {
        logger.info("GET /api/items/category/{} - Fetching items for user: {}", categoryId, authentication.getName());
        List<ItemDto> items = itemService.getItemsByCategory(categoryId, authentication.getName());
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/expiring")
    @Operation(summary = "Get expiring items", description = "Retrieve items expiring within specified days")
    public ResponseEntity<List<ItemDto>> getExpiringSoonItems(
            @Parameter(description = "Days ahead to check for expiry") @RequestParam(defaultValue = "" + AppConstants.DEFAULT_EXPIRY_WARNING_DAYS) int days,
            Authentication authentication) {
        
        logger.info("GET /api/items/expiring?days={} - Fetching expiring items for user: {}", days, authentication.getName());
        List<ItemDto> items = itemService.getExpiringSoonItems(days, authentication.getName());
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/expired")
    @Operation(summary = "Get expired items", description = "Retrieve all expired items")
    public ResponseEntity<List<ItemDto>> getExpiredItems(Authentication authentication) {
        logger.info("GET /api/items/expired - Fetching expired items for user: {}", authentication.getName());
        List<ItemDto> items = itemService.getExpiredItems(authentication.getName());
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/filter")
    @Operation(summary = "Get filtered items", description = "Retrieve items with multiple filter options")
    public ResponseEntity<Page<ItemDto>> getFilteredItems(
            @Valid ItemFilterRequest filter,
            Authentication authentication) {
        
        logger.info("GET /api/items/filter - Fetching filtered items for user: {} with filter: {}", 
                   authentication.getName(), filter);
        
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
        Page<ItemDto> items = itemService.getItemsWithFilters(
            authentication.getName(), filter.getCategoryId(), filter.getSearchTerm(), 
            filter.getExpiringSoon(), filter.getExpiryDays(), filter.getSortBy(), pageable
        );
        
        return ResponseEntity.ok(items);
    }
    
    // Statistics endpoints
    @GetMapping("/stats")
    @Operation(summary = "Get item statistics", description = "Retrieve statistics about user's items")
    public ResponseEntity<Map<String, Object>> getItemStatistics(Authentication authentication) {
        logger.info("GET /api/items/stats - Fetching statistics for user: {}", authentication.getName());
        
        String username = authentication.getName();
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalItems", itemService.getTotalItemsCount(username));
        stats.put("expiringSoon", itemService.getExpiringSoonCount(7, username));
        stats.put("expired", itemService.getExpiredCount(username));
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/expiring")
    @Operation(summary = "Get expiring items count", description = "Get count of items expiring within specified days")
    public ResponseEntity<Long> getExpiringSoonCount(
            @Parameter(description = "Days ahead to check") @RequestParam(defaultValue = "" + AppConstants.DEFAULT_EXPIRY_WARNING_DAYS) int days,
            Authentication authentication) {
        
        long count = itemService.getExpiringSoonCount(days, authentication.getName());
        return ResponseEntity.ok(count);
    }
}