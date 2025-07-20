package com.gefrierschrank.app.service;

import com.gefrierschrank.app.dto.CreateItemRequest;
import com.gefrierschrank.app.dto.ItemDto;
import com.gefrierschrank.app.dto.UpdateItemRequest;
import com.gefrierschrank.app.entity.Category;
import com.gefrierschrank.app.entity.ExpiryType;
import com.gefrierschrank.app.entity.Item;
import com.gefrierschrank.app.entity.User;
import com.gefrierschrank.app.repository.CategoryRepository;
import com.gefrierschrank.app.repository.ItemRepository;
import com.gefrierschrank.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemService itemService;

    private User testUser;
    private Category testCategory;
    private Item testItem;
    private CreateItemRequest createRequest;
    private UpdateItemRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(LocalDateTime.now());

        // Setup test category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Fleisch");
        testCategory.setIcon("meat");
        testCategory.setDefaultUnit("kg");
        testCategory.setUnitStep(new BigDecimal("0.1"));
        testCategory.setMinValue(new BigDecimal("0.1"));
        testCategory.setMaxValue(new BigDecimal("5.0"));

        // Setup test item
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Chicken Breast");
        testItem.setCategory(testCategory);
        testItem.setUser(testUser);
        testItem.setQuantity(new BigDecimal("1.0"));
        testItem.setUnit("kg");
        testItem.setExpiryDate(LocalDate.now().plusDays(5));
        testItem.setExpiryType(ExpiryType.BEST_BEFORE);
        testItem.setDescription("Fresh chicken breast");
        testItem.setCreatedAt(LocalDateTime.now());

        // Setup create request
        createRequest = new CreateItemRequest();
        createRequest.setName("Test Item");
        createRequest.setCategoryId(1L);
        createRequest.setQuantity(new BigDecimal("0.5"));
        createRequest.setUnit("kg");
        createRequest.setExpiryDate(LocalDate.now().plusDays(7));
        createRequest.setExpiryType(ExpiryType.BEST_BEFORE);
        createRequest.setDescription("Test description");

        // Setup update request
        updateRequest = new UpdateItemRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setCategoryId(1L);
        updateRequest.setQuantity(new BigDecimal("1.5"));
        updateRequest.setUnit("kg");
        updateRequest.setExpiryDate(LocalDate.now().plusDays(10));
        updateRequest.setExpiryType(ExpiryType.USE_BY);
        updateRequest.setDescription("Updated description");
    }

    @Test
    void getAllItemsByUser_ShouldReturnItemDtos() {
        // Given
        List<Item> items = Arrays.asList(testItem);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(itemRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(items);

        // When
        List<ItemDto> result = itemService.getAllItemsByUser("testuser");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Chicken Breast");
        verify(userRepository).findByUsername("testuser");
        verify(itemRepository).findByUserOrderByCreatedAtDesc(testUser);
    }

    @Test
    void getAllItemsByUser_UserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> itemService.getAllItemsByUser("nonexistent"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found with username: nonexistent");
    }

    @Test
    void getItemsByUserPaginated_ShouldReturnPagedItems() {
        // Given
        List<Item> items = Arrays.asList(testItem);
        Page<Item> itemPage = new PageImpl<>(items);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(itemRepository.findByUserOrderByCreatedAtDesc(testUser, pageable)).thenReturn(itemPage);

        // When
        Page<ItemDto> result = itemService.getItemsByUserPaginated("testuser", pageable);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Chicken Breast");
    }

    @Test
    void getItemById_ValidIdAndUser_ShouldReturnItemDto() {
        // Given
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // When
        ItemDto result = itemService.getItemById(1L, "testuser");

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Chicken Breast");
    }

    @Test
    void getItemById_ItemNotFound_ShouldThrowException() {
        // Given
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> itemService.getItemById(999L, "testuser"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Item not found with id: 999");
    }

    @Test
    void getItemById_WrongUser_ShouldThrowSecurityException() {
        // Given
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // When & Then
        assertThatThrownBy(() -> itemService.getItemById(1L, "wronguser"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("User is not authorized to access this item");
    }

    @Test
    void createItem_ValidRequest_ShouldCreateItem() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        // When
        ItemDto result = itemService.createItem(createRequest, "testuser");

        // Then
        assertThat(result).isNotNull();
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_CategoryNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> itemService.createItem(createRequest, "testuser"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found with id: 1");
    }

    @Test
    void createItem_InvalidQuantity_TooSmall_ShouldThrowException() {
        // Given
        createRequest.setQuantity(new BigDecimal("0.05")); // Below minimum
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When & Then
        assertThatThrownBy(() -> itemService.createItem(createRequest, "testuser"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity cannot be less than minimum value: 0.1");
    }

    @Test
    void createItem_InvalidQuantity_TooLarge_ShouldThrowException() {
        // Given
        createRequest.setQuantity(new BigDecimal("10.0")); // Above maximum
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When & Then
        assertThatThrownBy(() -> itemService.createItem(createRequest, "testuser"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity cannot be greater than maximum value: 5.0");
    }

    @Test
    void createItem_InvalidQuantity_WrongStep_ShouldThrowException() {
        // Given
        createRequest.setQuantity(new BigDecimal("1.15")); // Not aligned with 0.1 step
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When & Then
        assertThatThrownBy(() -> itemService.createItem(createRequest, "testuser"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be in steps of: 0.1");
    }

    @Test
    void updateItem_ValidRequest_ShouldUpdateItem() {
        // Given
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        // When
        ItemDto result = itemService.updateItem(1L, updateRequest, "testuser");

        // Then
        assertThat(result).isNotNull();
        verify(itemRepository).save(testItem);
    }

    @Test
    void deleteItem_ValidId_ShouldDeleteItem() {
        // Given
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // When
        itemService.deleteItem(1L, "testuser");

        // Then
        verify(itemRepository).delete(testItem);
    }

    @Test
    void searchItemsByName_ShouldReturnMatchingItems() {
        // Given
        List<Item> items = Arrays.asList(testItem);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(itemRepository.findByUserAndNameContainingIgnoreCase(testUser, "chicken"))
                .thenReturn(items);

        // When
        List<ItemDto> result = itemService.searchItemsByName("chicken", "testuser");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Chicken Breast");
    }

    @Test
    void getItemsByCategory_ValidCategory_ShouldReturnItems() {
        // Given
        List<Item> items = Arrays.asList(testItem);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(itemRepository.findByUserAndCategoryOrderByCreatedAtDesc(testUser, testCategory))
                .thenReturn(items);

        // When
        List<ItemDto> result = itemService.getItemsByCategory(1L, "testuser");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Chicken Breast");
    }

    @Test
    void getExpiringSoonItems_ShouldReturnItemsExpiringSoon() {
        // Given
        List<Item> items = Arrays.asList(testItem);
        LocalDate expiryDate = LocalDate.now().plusDays(7);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(itemRepository.findByUserAndExpiringSoon(testUser, expiryDate))
                .thenReturn(items);

        // When
        List<ItemDto> result = itemService.getExpiringSoonItems(7, "testuser");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Chicken Breast");
    }

    @Test
    void getExpiredItems_ShouldReturnExpiredItems() {
        // Given
        List<Item> items = Arrays.asList(testItem);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(itemRepository.findByUserAndExpired(testUser)).thenReturn(items);

        // When
        List<ItemDto> result = itemService.getExpiredItems("testuser");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Chicken Breast");
    }

    @Test
    void getTotalItemsCount_ShouldReturnCount() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(itemRepository.countByUser(testUser)).thenReturn(5L);

        // When
        long result = itemService.getTotalItemsCount("testuser");

        // Then
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void getExpiringSoonCount_ShouldReturnCount() {
        // Given
        LocalDate expiryDate = LocalDate.now().plusDays(7);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(itemRepository.countByUserAndExpiringSoon(testUser, expiryDate)).thenReturn(3L);

        // When
        long result = itemService.getExpiringSoonCount(7, "testuser");

        // Then
        assertThat(result).isEqualTo(3L);
    }

    @Test
    void getExpiredCount_ShouldReturnCount() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(itemRepository.countByUserAndExpired(testUser)).thenReturn(2L);

        // When
        long result = itemService.getExpiredCount("testuser");

        // Then
        assertThat(result).isEqualTo(2L);
    }

    @Test
    void isItemOwner_ValidOwner_ShouldReturnTrue() {
        // Given
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // When
        boolean result = itemService.isItemOwner(1L, "testuser");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isItemOwner_WrongOwner_ShouldReturnFalse() {
        // Given
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // When
        boolean result = itemService.isItemOwner(1L, "wronguser");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isItemOwner_ItemNotFound_ShouldReturnFalse() {
        // Given
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean result = itemService.isItemOwner(999L, "testuser");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getItemsWithFilters_AllFilters_ShouldReturnFilteredItems() {
        // Given
        List<Item> items = Arrays.asList(testItem);
        Page<Item> itemPage = new PageImpl<>(items);
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate expiryDate = LocalDate.now().plusDays(7);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(itemRepository.findByUserWithFilters(testUser, testCategory, "chicken", 
                true, expiryDate, "name", pageable)).thenReturn(itemPage);

        // When
        Page<ItemDto> result = itemService.getItemsWithFilters("testuser", 1L, "chicken", 
                true, 7, "name", pageable);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Chicken Breast");
    }
}