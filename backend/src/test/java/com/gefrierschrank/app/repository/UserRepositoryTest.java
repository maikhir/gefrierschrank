package com.gefrierschrank.app.repository;

import com.gefrierschrank.app.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "hashedPassword", User.Role.USER);
        adminUser = new User("admin", "admin@example.com", "hashedPassword", User.Role.ADMIN);
    }

    @Test
    void findByUsername_UserExists_ReturnsUser() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> found = userRepository.findByUsername("testuser");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByUsername_UserNotExists_ReturnsEmpty() {
        // Act
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_UserExists_ReturnsUser() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByEmail_UserNotExists_ReturnsEmpty() {
        // Act
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void existsByUsername_UserExists_ReturnsTrue() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        boolean exists = userRepository.existsByUsername("testuser");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByUsername_UserNotExists_ReturnsFalse() {
        // Act
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByEmail_UserExists_ReturnsTrue() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_UserNotExists_ReturnsFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByRole_UsersExist_ReturnsUsers() {
        // Arrange
        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(adminUser);

        // Act
        List<User> users = userRepository.findByRole(User.Role.USER);
        List<User> admins = userRepository.findByRole(User.Role.ADMIN);

        // Assert
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getUsername());
        
        assertEquals(1, admins.size());
        assertEquals("admin", admins.get(0).getUsername());
    }

    @Test
    void countAdmins_AdminsExist_ReturnsCount() {
        // Arrange
        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(adminUser);

        // Act
        long adminCount = userRepository.countAdmins();

        // Assert
        assertEquals(1, adminCount);
    }

    @Test
    void countAdmins_NoAdmins_ReturnsZero() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        long adminCount = userRepository.countAdmins();

        // Assert
        assertEquals(0, adminCount);
    }

    @Test
    void save_NewUser_PersistsUser() {
        // Act
        User savedUser = userRepository.save(testUser);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    void delete_ExistingUser_RemovesUser() {
        // Arrange
        User persistedUser = entityManager.persistAndFlush(testUser);
        Long userId = persistedUser.getId();

        // Act
        userRepository.delete(persistedUser);
        entityManager.flush();

        // Assert
        Optional<User> found = userRepository.findById(userId);
        assertFalse(found.isPresent());
    }
}