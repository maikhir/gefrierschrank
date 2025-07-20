package com.gefrierschrank.app.service;

import com.gefrierschrank.app.dto.UserDto;
import com.gefrierschrank.app.entity.User;
import com.gefrierschrank.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setRole(User.Role.USER);
        testUser.setNotificationsEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDto result = userService.createUser("newuser", "new@example.com", "password123", User.Role.USER);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(User.Role.USER, result.getRole());
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            userService.createUser("existinguser", "new@example.com", "password123", User.Role.USER)
        );

        assertEquals("Error: Username is already taken!", exception.getMessage());
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            userService.createUser("newuser", "existing@example.com", "password123", User.Role.USER)
        );

        assertEquals("Error: Email is already in use!", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUsername_UserExists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserDto> result = userService.findByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByUsername_UserNotExists() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<UserDto> result = userService.findByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void findById_UserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserDto> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void findAllUsers() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setRole(User.Role.ADMIN);
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // Act
        List<UserDto> result = userService.findAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("testuser2", result.get(1).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto updateDto = new UserDto();
        updateDto.setUsername("updateduser");
        updateDto.setEmail("updated@example.com");
        updateDto.setRole(User.Role.ADMIN);

        // Act
        UserDto result = userService.updateUser(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        testUser.setRole(User.Role.USER); // Not an admin
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_LastAdmin_ThrowsException() {
        // Arrange
        testUser.setRole(User.Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.countAdmins()).thenReturn(1L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            userService.deleteUser(1L)
        );

        assertEquals("Cannot delete the last admin user", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository).countAdmins();
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void existsByUsername() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean result = userService.existsByUsername("testuser");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void existsByEmail() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = userService.existsByEmail("test@example.com");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }
}