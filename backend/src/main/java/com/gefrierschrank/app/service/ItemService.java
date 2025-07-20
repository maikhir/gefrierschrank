package com.gefrierschrank.app.service;

import com.gefrierschrank.app.dto.CreateItemRequest;
import com.gefrierschrank.app.dto.ItemDto;
import com.gefrierschrank.app.dto.UpdateItemRequest;
import com.gefrierschrank.app.entity.Category;
import com.gefrierschrank.app.entity.Item;
import com.gefrierschrank.app.entity.User;
import com.gefrierschrank.app.repository.CategoryRepository;
import com.gefrierschrank.app.repository.ItemRepository;
import com.gefrierschrank.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemService {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    
    public ItemService(ItemRepository itemRepository, 
                      CategoryRepository categoryRepository,
                      UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional(readOnly = true)
    public List<ItemDto> getAllItemsByUser(String username) {
        logger.debug("Fetching all items for user: {}", username);
        User user = getUserByUsername(username);
        return itemRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(ItemDto::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<ItemDto> getItemsByUserPaginated(String username, Pageable pageable) {
        logger.debug("Fetching paginated items for user: {}", username);
        User user = getUserByUsername(username);
        return itemRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(ItemDto::new);
    }
    
    @Transactional(readOnly = true)
    public ItemDto getItemById(Long id, String username) {
        logger.debug("Fetching item with id: {} for user: {}", id, username);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        
        // Ensure user can only access their own items
        if (!item.getUser().getUsername().equals(username)) {
            throw new SecurityException("User is not authorized to access this item");
        }
        
        return new ItemDto(item);
    }
    
    public ItemDto createItem(CreateItemRequest request, String username) {
        logger.info("Creating new item: {} for user: {}", request.getName(), username);
        
        User user = getUserByUsername(username);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        // Validate quantity against category constraints
        validateQuantityConstraints(request.getQuantity(), category);
        
        Item item = new Item();
        item.setName(request.getName());
        item.setCategory(category);
        item.setUser(user);
        item.setQuantity(request.getQuantity());
        item.setUnit(request.getUnit());
        item.setExpiryDate(request.getExpiryDate());
        item.setExpiryType(request.getExpiryType());
        item.setPhotoPath(request.getPhotoPath());
        item.setDescription(request.getDescription());
        
        item = itemRepository.save(item);
        
        logger.info("Item created successfully with id: {}", item.getId());
        return new ItemDto(item);
    }
    
    @PreAuthorize("@itemService.isItemOwner(#id, authentication.name)")
    public ItemDto updateItem(Long id, UpdateItemRequest request, String username) {
        logger.info("Updating item with id: {} for user: {}", id, username);
        
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        // Validate quantity against category constraints
        validateQuantityConstraints(request.getQuantity(), category);
        
        // Update fields
        existingItem.setName(request.getName());
        existingItem.setCategory(category);
        existingItem.setQuantity(request.getQuantity());
        existingItem.setUnit(request.getUnit());
        existingItem.setExpiryDate(request.getExpiryDate());
        existingItem.setExpiryType(request.getExpiryType());
        existingItem.setPhotoPath(request.getPhotoPath());
        existingItem.setDescription(request.getDescription());
        
        existingItem = itemRepository.save(existingItem);
        
        logger.info("Item updated successfully with id: {}", existingItem.getId());
        return new ItemDto(existingItem);
    }
    
    @PreAuthorize("@itemService.isItemOwner(#id, authentication.name)")
    public void deleteItem(Long id, String username) {
        logger.info("Deleting item with id: {} for user: {}", id, username);
        
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));
        
        itemRepository.delete(item);
        logger.info("Item deleted successfully with id: {}", id);
    }
    
    // Search and filter methods
    @Transactional(readOnly = true)
    public List<ItemDto> searchItemsByName(String searchTerm, String username) {
        logger.debug("Searching items with term: {} for user: {}", searchTerm, username);
        User user = getUserByUsername(username);
        return itemRepository.findByUserAndNameContainingIgnoreCase(user, searchTerm)
                .stream()
                .map(ItemDto::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ItemDto> getItemsByCategory(Long categoryId, String username) {
        logger.debug("Fetching items by category: {} for user: {}", categoryId, username);
        User user = getUserByUsername(username);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        
        return itemRepository.findByUserAndCategoryOrderByCreatedAtDesc(user, category)
                .stream()
                .map(ItemDto::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ItemDto> getExpiringSoonItems(int days, String username) {
        logger.debug("Fetching items expiring within {} days for user: {}", days, username);
        User user = getUserByUsername(username);
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        
        return itemRepository.findByUserAndExpiringSoon(user, expiryDate)
                .stream()
                .map(ItemDto::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ItemDto> getExpiredItems(String username) {
        logger.debug("Fetching expired items for user: {}", username);
        User user = getUserByUsername(username);
        
        return itemRepository.findByUserAndExpired(user)
                .stream()
                .map(ItemDto::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<ItemDto> getItemsWithFilters(String username, Long categoryId, String searchTerm, 
                                           Boolean expiringSoon, Integer expiryDays, String sortBy, 
                                           Pageable pageable) {
        logger.debug("Fetching filtered items for user: {}", username);
        
        User user = getUserByUsername(username);
        Category category = categoryId != null ? 
            categoryRepository.findById(categoryId).orElse(null) : null;
        
        boolean hasExpiryFilter = Boolean.TRUE.equals(expiringSoon);
        LocalDate expiryDate = hasExpiryFilter ? 
            LocalDate.now().plusDays(expiryDays != null ? expiryDays : 7) : null;
        
        return itemRepository.findByUserWithFilters(
            user, category, searchTerm, hasExpiryFilter, expiryDate, sortBy, pageable
        ).map(ItemDto::new);
    }
    
    // Statistics methods
    @Transactional(readOnly = true)
    public long getTotalItemsCount(String username) {
        User user = getUserByUsername(username);
        return itemRepository.countByUser(user);
    }
    
    @Transactional(readOnly = true)
    public long getExpiringSoonCount(int days, String username) {
        User user = getUserByUsername(username);
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        return itemRepository.countByUserAndExpiringSoon(user, expiryDate);
    }
    
    @Transactional(readOnly = true)
    public long getExpiredCount(String username) {
        User user = getUserByUsername(username);
        return itemRepository.countByUserAndExpired(user);
    }
    
    // Helper methods
    public boolean isItemOwner(Long itemId, String username) {
        return itemRepository.findById(itemId)
                .map(item -> item.getUser().getUsername().equals(username))
                .orElse(false);
    }
    
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }
    
    private void validateQuantityConstraints(java.math.BigDecimal quantity, Category category) {
        if (quantity.compareTo(category.getMinValue()) < 0) {
            throw new IllegalArgumentException("Quantity cannot be less than minimum value: " + category.getMinValue());
        }
        
        if (quantity.compareTo(category.getMaxValue()) > 0) {
            throw new IllegalArgumentException("Quantity cannot be greater than maximum value: " + category.getMaxValue());
        }
        
        // Check if quantity aligns with unit step
        java.math.BigDecimal remainder = quantity.remainder(category.getUnitStep());
        if (remainder.compareTo(java.math.BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Quantity must be in steps of: " + category.getUnitStep());
        }
    }
}