package com.gefrierschrank.app.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // Set test values using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "myTestSecretKey12345678901234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000); // 1 hour
    }

    @Test
    void generateJwtToken_WithUsername_Success() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtUtils.generateJwtToken(username);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.startsWith("eyJ")); // JWT tokens start with this
    }

    @Test
    void getUserNameFromJwtToken_ValidToken_ReturnsUsername() {
        // Arrange
        String username = "testuser";
        String token = jwtUtils.generateJwtToken(username);

        // Act
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateJwtToken_ValidToken_ReturnsTrue() {
        // Arrange
        String username = "testuser";
        String token = jwtUtils.generateJwtToken(username);

        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateJwtToken_InvalidToken_ReturnsFalse() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_EmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtUtils.validateJwtToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void generateAndValidateToken_EndToEndTest() {
        // Arrange
        String username = "endtoendtest";

        // Act
        String token = jwtUtils.generateJwtToken(username);
        boolean isValid = jwtUtils.validateJwtToken(token);
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);

        // Assert
        assertTrue(isValid);
        assertEquals(username, extractedUsername);
    }
}