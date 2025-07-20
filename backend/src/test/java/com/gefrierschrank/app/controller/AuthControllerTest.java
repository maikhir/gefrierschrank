package com.gefrierschrank.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gefrierschrank.app.dto.JwtResponse;
import com.gefrierschrank.app.dto.LoginRequest;
import com.gefrierschrank.app.security.JwtUtils;
import com.gefrierschrank.app.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest validLoginRequest;
    private UserPrincipal testUserPrincipal;
    private Authentication testAuthentication;

    @BeforeEach
    void setUp() {
        // Setup valid login request
        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("testuser");
        validLoginRequest.setPassword("testpassword");

        // Setup test user principal
        List<SimpleGrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        testUserPrincipal = new UserPrincipal(1L, "testuser", "test@example.com", "encodedpassword", authorities);

        // Setup test authentication
        testAuthentication = new UsernamePasswordAuthenticationToken(
            testUserPrincipal, "testpassword", authorities
        );
    }

    @Test
    void authenticateUser_ValidCredentials_ShouldReturnJwtResponse() throws Exception {
        // Given
        String expectedJwt = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYzOTc0OTYwMCwiZXhwIjoxNjM5ODM2MDAwfQ.test";
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(testAuthentication);
        when(jwtUtils.generateJwtToken(testAuthentication)).thenReturn(expectedJwt);

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(expectedJwt))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(testAuthentication);
    }

    @Test
    void authenticateUser_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    void authenticateUser_EmptyUsername_ShouldReturnBadRequest() throws Exception {
        // Given
        validLoginRequest.setUsername("");

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    void authenticateUser_EmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        validLoginRequest.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    void authenticateUser_NullUsername_ShouldReturnBadRequest() throws Exception {
        // Given
        validLoginRequest.setUsername(null);

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    void authenticateUser_NullPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        validLoginRequest.setPassword(null);

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    void authenticateUser_InvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    void authenticateUser_MissingContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnsupportedMediaType());

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    void authenticateUser_WithoutCsrf_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isForbidden());

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtils, never()).generateJwtToken(any());
    }

    @Test
    void authenticateUser_AdminUser_ShouldReturnJwtResponseWithAdminRole() throws Exception {
        // Given
        List<SimpleGrantedAuthority> adminAuthorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ROLE_USER")
        );
        UserPrincipal adminUserPrincipal = new UserPrincipal(
            2L, "admin", "admin@example.com", "encodedpassword", adminAuthorities
        );
        Authentication adminAuthentication = new UsernamePasswordAuthenticationToken(
            adminUserPrincipal, "adminpassword", adminAuthorities
        );
        String expectedJwt = "admin.jwt.token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(adminAuthentication);
        when(jwtUtils.generateJwtToken(adminAuthentication)).thenReturn(expectedJwt);

        // When & Then
        mockMvc.perform(post("/api/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(expectedJwt))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.containsInAnyOrder("ROLE_ADMIN", "ROLE_USER")));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(adminAuthentication);
    }
}