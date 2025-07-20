package com.gefrierschrank.app.repository;

import com.gefrierschrank.app.entity.Category;
import com.gefrierschrank.app.entity.ExpiryType;
import com.gefrierschrank.app.entity.Item;
import com.gefrierschrank.app.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    private User testUser;
    private User otherUser;
    private Category meatCategory;
    private Category vegetableCategory;
    private Item item1, item2, item3, item4, item5;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(User.Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(testUser);

        otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setRole(User.Role.USER);
        otherUser.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(otherUser);

        // Create test categories
        meatCategory = new Category();
        meatCategory.setName("Fleisch");
        meatCategory.setIcon("meat");
        meatCategory.setDefaultUnit("kg");
        meatCategory.setUnitStep(new BigDecimal("0.1"));
        meatCategory.setMinValue(new BigDecimal("0.1"));
        meatCategory.setMaxValue(new BigDecimal("5.0"));
        entityManager.persistAndFlush(meatCategory);

        vegetableCategory = new Category();
        vegetableCategory.setName("Gemüse");
        vegetableCategory.setIcon("vegetable");
        vegetableCategory.setDefaultUnit("g");
        vegetableCategory.setUnitStep(new BigDecimal("50"));
        vegetableCategory.setMinValue(new BigDecimal("50"));
        vegetableCategory.setMaxValue(new BigDecimal("2000"));
        entityManager.persistAndFlush(vegetableCategory);

        // Create test items
        item1 = createItem("Chicken Breast", meatCategory, testUser, LocalDate.now().plusDays(3), LocalDateTime.now().minusHours(1));
        item2 = createItem("Beef Steak", meatCategory, testUser, LocalDate.now().plusDays(7), LocalDateTime.now().minusHours(2));
        item3 = createItem("Carrots", vegetableCategory, testUser, LocalDate.now().minusDays(1), LocalDateTime.now().minusHours(3)); // Expired
        item4 = createItem("Broccoli", vegetableCategory, testUser, LocalDate.now().plusDays(14), LocalDateTime.now().minusHours(4));
        item5 = createItem("Other User Item", meatCategory, otherUser, LocalDate.now().plusDays(5), LocalDateTime.now().minusHours(5));

        entityManager.persistAndFlush(item1);
        entityManager.persistAndFlush(item2);
        entityManager.persistAndFlush(item3);
        entityManager.persistAndFlush(item4);
        entityManager.persistAndFlush(item5);
    }

    private Item createItem(String name, Category category, User user, LocalDate expiryDate, LocalDateTime createdAt) {
        Item item = new Item();
        item.setName(name);
        item.setCategory(category);
        item.setUser(user);
        item.setQuantity(new BigDecimal("1.0"));
        item.setUnit(category.getDefaultUnit());
        item.setExpiryDate(expiryDate);
        item.setExpiryType(ExpiryType.BEST_BEFORE);
        item.setDescription("Test description");
        item.setCreatedAt(createdAt);
        return item;
    }

    @Test
    void findByUserOrderByCreatedAtDesc_ShouldReturnUserItemsInDescOrder() {
        // When
        List<Item> items = itemRepository.findByUserOrderByCreatedAtDesc(testUser);

        // Then
        assertThat(items).hasSize(4);
        assertThat(items.get(0).getName()).isEqualTo("Chicken Breast"); // Most recent
        assertThat(items.get(1).getName()).isEqualTo("Beef Steak");
        assertThat(items.get(2).getName()).isEqualTo("Carrots");
        assertThat(items.get(3).getName()).isEqualTo("Broccoli"); // Oldest
        
        // Should not contain other user's items
        assertThat(items).noneMatch(item -> item.getName().equals("Other User Item"));
    }

    @Test
    void findByUserOrderByCreatedAtDesc_WithPageable_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Item> itemPage = itemRepository.findByUserOrderByCreatedAtDesc(testUser, pageable);

        // Then
        assertThat(itemPage.getContent()).hasSize(2);
        assertThat(itemPage.getTotalElements()).isEqualTo(4);
        assertThat(itemPage.getTotalPages()).isEqualTo(2);
        assertThat(itemPage.getContent().get(0).getName()).isEqualTo("Chicken Breast");
        assertThat(itemPage.getContent().get(1).getName()).isEqualTo("Beef Steak");
    }

    @Test
    void findByUserAndCategoryOrderByCreatedAtDesc_ShouldReturnCategorySpecificItems() {
        // When
        List<Item> meatItems = itemRepository.findByUserAndCategoryOrderByCreatedAtDesc(testUser, meatCategory);

        // Then
        assertThat(meatItems).hasSize(2);
        assertThat(meatItems.get(0).getName()).isEqualTo("Chicken Breast");
        assertThat(meatItems.get(1).getName()).isEqualTo("Beef Steak");
        assertThat(meatItems).allMatch(item -> item.getCategory().equals(meatCategory));
    }

    @Test
    void findByUserAndNameContainingIgnoreCase_ShouldReturnMatchingItems() {
        // When
        List<Item> items = itemRepository.findByUserAndNameContainingIgnoreCase(testUser, "beef");

        // Then
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("Beef Steak");
    }

    @Test
    void findByUserAndNameContainingIgnoreCase_CaseInsensitive_ShouldWork() {
        // When
        List<Item> items = itemRepository.findByUserAndNameContainingIgnoreCase(testUser, "CHICKEN");

        // Then
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("Chicken Breast");
    }

    @Test
    void findByUserAndExpiringSoon_ShouldReturnItemsExpiringSoon() {
        // Given
        LocalDate expiryDate = LocalDate.now().plusDays(7);

        // When
        List<Item> expiring = itemRepository.findByUserAndExpiringSoon(testUser, expiryDate);

        // Then
        assertThat(expiring).hasSize(2);
        assertThat(expiring).extracting(Item::getName)
                .containsExactly("Chicken Breast", "Beef Steak"); // Ordered by expiry date ASC
        assertThat(expiring).allMatch(item -> 
                item.getExpiryDate().isAfter(LocalDate.now().minusDays(1)) && 
                item.getExpiryDate().isBefore(expiryDate.plusDays(1)));
    }

    @Test
    void findByUserAndExpired_ShouldReturnExpiredItems() {
        // When
        List<Item> expired = itemRepository.findByUserAndExpired(testUser);

        // Then
        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getName()).isEqualTo("Carrots");
        assertThat(expired.get(0).getExpiryDate()).isBefore(LocalDate.now());
    }

    @Test
    void findByUserWithFilters_AllFilters_ShouldReturnFilteredResults() {
        // Given
        LocalDate expiryDate = LocalDate.now().plusDays(10);
        Pageable pageable = PageRequest.of(0, 10);

        // When - Filter by meat category, name containing "beef", expiring soon
        Page<Item> filtered = itemRepository.findByUserWithFilters(
                testUser, meatCategory, "beef", true, expiryDate, "name", pageable);

        // Then
        assertThat(filtered.getContent()).hasSize(1);
        assertThat(filtered.getContent().get(0).getName()).isEqualTo("Beef Steak");
    }

    @Test
    void findByUserWithFilters_NoFilters_ShouldReturnAllUserItems() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - No filters applied
        Page<Item> all = itemRepository.findByUserWithFilters(
                testUser, null, null, false, null, "created", pageable);

        // Then
        assertThat(all.getContent()).hasSize(4);
        assertThat(all.getContent().get(0).getName()).isEqualTo("Chicken Breast"); // Most recent
    }

    @Test
    void findByUserWithFilters_SortByName_ShouldSortCorrectly() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Item> sorted = itemRepository.findByUserWithFilters(
                testUser, null, null, false, null, "name", pageable);

        // Then
        assertThat(sorted.getContent()).hasSize(4);
        assertThat(sorted.getContent().get(0).getName()).isEqualTo("Beef Steak"); // Alphabetically first
        assertThat(sorted.getContent().get(1).getName()).isEqualTo("Broccoli");
        assertThat(sorted.getContent().get(2).getName()).isEqualTo("Carrots");
        assertThat(sorted.getContent().get(3).getName()).isEqualTo("Chicken Breast");
    }

    @Test
    void countByUser_ShouldReturnCorrectCount() {
        // When
        long count = itemRepository.countByUser(testUser);

        // Then
        assertThat(count).isEqualTo(4);
    }

    @Test
    void countByUserAndCategory_ShouldReturnCategoryCount() {
        // When
        long meatCount = itemRepository.countByUserAndCategory(testUser, meatCategory);
        long vegetableCount = itemRepository.countByUserAndCategory(testUser, vegetableCategory);

        // Then
        assertThat(meatCount).isEqualTo(2);
        assertThat(vegetableCount).isEqualTo(2);
    }

    @Test
    void countByUserAndExpiringSoon_ShouldReturnExpiringCount() {
        // Given
        LocalDate expiryDate = LocalDate.now().plusDays(7);

        // When
        long count = itemRepository.countByUserAndExpiringSoon(testUser, expiryDate);

        // Then
        assertThat(count).isEqualTo(2); // Chicken Breast and Beef Steak
    }

    @Test
    void countByUserAndExpired_ShouldReturnExpiredCount() {
        // When
        long count = itemRepository.countByUserAndExpired(testUser);

        // Then
        assertThat(count).isEqualTo(1); // Only Carrots
    }

    @Test
    void findCategoryStatsByUser_ShouldReturnCategoryStatistics() {
        // When
        List<Object[]> stats = itemRepository.findCategoryStatsByUser(testUser);

        // Then
        assertThat(stats).hasSize(2);
        
        // Both categories have 2 items each, check that both are present
        assertThat(stats).anySatisfy(stat -> {
            assertThat(stat[0]).isIn("Fleisch", "Gemüse");
            assertThat(stat[1]).isEqualTo(2L);
        });
    }

    @Test
    void findRecentItemsByUser_WithPageable_ShouldReturnRecentItems() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        List<Item> recent = itemRepository.findRecentItemsByUser(testUser, pageable);

        // Then
        assertThat(recent).hasSize(2);
        assertThat(recent.get(0).getName()).isEqualTo("Chicken Breast");
        assertThat(recent.get(1).getName()).isEqualTo("Beef Steak");
    }

    @Test
    void findByUserAndExpiryDateBetween_ShouldReturnItemsInDateRange() {
        // Given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(10);

        // When
        List<Item> items = itemRepository.findByUserAndExpiryDateBetween(testUser, startDate, endDate);

        // Then
        assertThat(items).hasSize(2);
        assertThat(items).extracting(Item::getName)
                .containsExactly("Chicken Breast", "Beef Steak"); // Ordered by expiry date ASC
    }

    @Test
    void findByUserAndExpiryDateBefore_ShouldReturnItemsBeforeDate() {
        // Given
        LocalDate date = LocalDate.now().plusDays(5);

        // When
        List<Item> items = itemRepository.findByUserAndExpiryDateBefore(testUser, date);

        // Then
        assertThat(items).hasSize(2);
        assertThat(items).extracting(Item::getName)
                .containsExactly("Carrots", "Chicken Breast"); // Ordered by expiry date ASC
    }

    @Test
    void repository_ShouldOnlyReturnDataForCorrectUser() {
        // Test that all queries properly filter by user
        
        // When
        List<Item> testUserItems = itemRepository.findByUserOrderByCreatedAtDesc(testUser);
        List<Item> otherUserItems = itemRepository.findByUserOrderByCreatedAtDesc(otherUser);

        // Then
        assertThat(testUserItems).hasSize(4);
        assertThat(otherUserItems).hasSize(1);
        assertThat(otherUserItems.get(0).getName()).isEqualTo("Other User Item");
        
        // Cross-check - no overlap
        assertThat(testUserItems).noneMatch(item -> item.getUser().equals(otherUser));
        assertThat(otherUserItems).noneMatch(item -> item.getUser().equals(testUser));
    }
}