package com.gefrierschrank.app.security;

import com.gefrierschrank.app.entity.User;
import com.gefrierschrank.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void loadUserByUsername_ExistingUser_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(UserPrincipal.class);
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        
        // Check authorities
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_AdminUser_ShouldReturnUserDetailsWithAdminRole() {
        // Given
        testUser.setRole(User.Role.ADMIN);
        when(userRepository.findByUsername("adminuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("adminuser");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
        
        verify(userRepository).findByUsername("adminuser");
    }

    @Test
    void loadUserByUsername_NonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User Not Found with username: nonexistent");
        
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void loadUserByUsername_NullUsername_ShouldCallRepositoryWithNull() {
        // Given
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User Not Found with username: null");
        
        verify(userRepository).findByUsername(null);
    }

    @Test
    void loadUserByUsername_EmptyUsername_ShouldCallRepositoryWithEmptyString() {
        // Given
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User Not Found with username: ");
        
        verify(userRepository).findByUsername("");
    }

    @Test
    void loadUserByUsername_UserWithNullRole_ShouldHandleNullRole() {
        // Given
        testUser.setRole(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_UserWithEmptyRole_ShouldReturnEmptyRole() {
        // Given
        // Skip this test as empty string cannot be set to enum
        // This test is not applicable with enum-based roles
    }

    @Test
    void loadUserByUsername_UserPrincipalHasCorrectProperties() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;

        // Then
        assertThat(userPrincipal.getId()).isEqualTo(1L);
        assertThat(userPrincipal.getUsername()).isEqualTo("testuser");
        assertThat(userPrincipal.getEmail()).isEqualTo("test@example.com");
        assertThat(userPrincipal.getPassword()).isEqualTo("encodedPassword");
        
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_CaseInsensitive_ShouldWorkCorrectly() {
        // Given - Repository is responsible for case handling, not the service
        when(userRepository.findByUsername("TestUser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("TestUser");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser"); // Returns actual username from DB
        
        verify(userRepository).findByUsername("TestUser");
    }

    @Test
    void loadUserByUsername_RepositoryThrowsException_ShouldPropagateException() {
        // Given
        when(userRepository.findByUsername("testuser")).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
        
        verify(userRepository).findByUsername("testuser");
    }
}