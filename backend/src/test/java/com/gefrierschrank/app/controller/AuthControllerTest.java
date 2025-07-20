package com.gefrierschrank.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gefrierschrank.app.dto.LoginRequest;
import com.gefrierschrank.app.dto.UserDto;
import com.gefrierschrank.app.entity.User;
import com.gefrierschrank.app.security.JwtUtils;
import com.gefrierschrank.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE))
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void signin_ValidCredentials_ReturnsToken() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        UserDto userDto = createTestUserDto();
        
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(auth);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("test-jwt-token");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(userDto));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(auth);
        verify(userService).findByUsername("testuser");
    }

    @Test
    void signin_InvalidCredentials_ReturnsBadRequest() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Invalid username or password!"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtils, userService);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUser_AuthenticatedUser_ReturnsUser() throws Exception {
        // Arrange
        UserDto userDto = createTestUserDto();
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(userDto));

        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).findByUsername("testuser");
    }

    @Test
    void getCurrentUser_NotAuthenticated_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: No authentication found!"));

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "testuser")
    void refreshToken_AuthenticatedUser_ReturnsNewToken() throws Exception {
        // Arrange
        UserDto userDto = createTestUserDto();
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(userDto));
        when(jwtUtils.generateJwtToken("testuser")).thenReturn("new-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"));

        verify(userService).findByUsername("testuser");
        verify(jwtUtils).generateJwtToken("testuser");
    }

    private UserDto createTestUserDto() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");
        userDto.setRole(User.Role.USER);
        userDto.setNotificationsEnabled(true);
        userDto.setCreatedAt(LocalDateTime.now());
        return userDto;
    }
}