package com.gefrierschrank.app.repository;

import com.gefrierschrank.app.entity.Item;
import com.gefrierschrank.app.entity.Category;
import com.gefrierschrank.app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    // Basic queries by user
    List<Item> findByUserOrderByCreatedAtDesc(User user);
    
    Page<Item> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Filter by category
    List<Item> findByUserAndCategoryOrderByCreatedAtDesc(User user, Category category);
    
    Page<Item> findByUserAndCategoryOrderByCreatedAtDesc(User user, Category category, Pageable pageable);
    
    // Search by name
    @Query("SELECT i FROM Item i WHERE i.user = :user AND " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY i.createdAt DESC")
    List<Item> findByUserAndNameContainingIgnoreCase(@Param("user") User user, 
                                                     @Param("searchTerm") String searchTerm);
    
    @Query("SELECT i FROM Item i WHERE i.user = :user AND " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY i.createdAt DESC")
    Page<Item> findByUserAndNameContainingIgnoreCase(@Param("user") User user, 
                                                     @Param("searchTerm") String searchTerm, 
                                                     Pageable pageable);
    
    // Expiry date queries
    @Query("SELECT i FROM Item i WHERE i.user = :user AND i.expiryDate IS NOT NULL AND " +
           "i.expiryDate BETWEEN :startDate AND :endDate ORDER BY i.expiryDate ASC")
    List<Item> findByUserAndExpiryDateBetween(@Param("user") User user, 
                                             @Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    @Query("SELECT i FROM Item i WHERE i.user = :user AND i.expiryDate IS NOT NULL AND " +
           "i.expiryDate <= :date ORDER BY i.expiryDate ASC")
    List<Item> findByUserAndExpiryDateBefore(@Param("user") User user, @Param("date") LocalDate date);
    
    // Items expiring within specified days
    @Query("SELECT i FROM Item i WHERE i.user = :user AND i.expiryDate IS NOT NULL AND " +
           "i.expiryDate BETWEEN CURRENT_DATE AND :expiryDate ORDER BY i.expiryDate ASC")
    List<Item> findByUserAndExpiringSoon(@Param("user") User user, @Param("expiryDate") LocalDate expiryDate);
    
    // Items already expired
    @Query("SELECT i FROM Item i WHERE i.user = :user AND i.expiryDate IS NOT NULL AND " +
           "i.expiryDate < CURRENT_DATE ORDER BY i.expiryDate DESC")
    List<Item> findByUserAndExpired(@Param("user") User user);
    
    // Combined filters
    @Query("SELECT i FROM Item i WHERE i.user = :user " +
           "AND (:category IS NULL OR i.category = :category) " +
           "AND (:searchTerm IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:hasExpiryFilter = false OR (i.expiryDate IS NOT NULL AND i.expiryDate <= :expiryDate)) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'name' THEN i.name END ASC, " +
           "CASE WHEN :sortBy = 'expiry' THEN i.expiryDate END ASC, " +
           "CASE WHEN :sortBy = 'category' THEN i.category.name END ASC, " +
           "CASE WHEN :sortBy = 'quantity' THEN i.quantity END DESC, " +
           "i.createdAt DESC")
    Page<Item> findByUserWithFilters(@Param("user") User user,
                                    @Param("category") Category category,
                                    @Param("searchTerm") String searchTerm,
                                    @Param("hasExpiryFilter") boolean hasExpiryFilter,
                                    @Param("expiryDate") LocalDate expiryDate,
                                    @Param("sortBy") String sortBy,
                                    Pageable pageable);
    
    // Statistics
    @Query("SELECT COUNT(i) FROM Item i WHERE i.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(i) FROM Item i WHERE i.user = :user AND i.category = :category")
    long countByUserAndCategory(@Param("user") User user, @Param("category") Category category);
    
    @Query("SELECT COUNT(i) FROM Item i WHERE i.user = :user AND i.expiryDate IS NOT NULL AND " +
           "i.expiryDate BETWEEN CURRENT_DATE AND :expiryDate")
    long countByUserAndExpiringSoon(@Param("user") User user, @Param("expiryDate") LocalDate expiryDate);
    
    @Query("SELECT COUNT(i) FROM Item i WHERE i.user = :user AND i.expiryDate IS NOT NULL AND " +
           "i.expiryDate < CURRENT_DATE")
    long countByUserAndExpired(@Param("user") User user);
    
    // Category statistics
    @Query("SELECT i.category.name, COUNT(i) FROM Item i WHERE i.user = :user GROUP BY i.category.name ORDER BY COUNT(i) DESC")
    List<Object[]> findCategoryStatsByUser(@Param("user") User user);
    
    // Recent items
    @Query("SELECT i FROM Item i WHERE i.user = :user ORDER BY i.createdAt DESC")
    List<Item> findRecentItemsByUser(@Param("user") User user, Pageable pageable);
}