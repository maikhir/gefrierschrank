package com.gefrierschrank.app.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private String jwtSecret = "myVerySecureSecretKeyThatIsLongEnoughForJWTHMACAlgorithmAndMeetsThe256BitRequirement";
    private int jwtExpirationMs = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", jwtExpirationMs);
    }

    @Test
    void generateJwtToken_ValidAuthentication_ShouldGenerateValidToken() {
        // Given
        List<SimpleGrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        UserPrincipal userPrincipal = new UserPrincipal(1L, "testuser", "test@example.com", 
                "encodedpassword", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, authorities);

        // When
        String token = jwtUtils.generateJwtToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }

    @Test
    void getUserNameFromJwtToken_ValidToken_ShouldReturnUsername() {
        // Given
        List<SimpleGrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        UserPrincipal userPrincipal = new UserPrincipal(1L, "testuser", "test@example.com", 
                "encodedpassword", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, authorities);
        String token = jwtUtils.generateJwtToken(authentication);

        // When
        String username = jwtUtils.getUserNameFromJwtToken(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void validateJwtToken_ValidToken_ShouldReturnTrue() {
        // Given
        List<SimpleGrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        UserPrincipal userPrincipal = new UserPrincipal(1L, "testuser", "test@example.com", 
                "encodedpassword", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, authorities);
        String token = jwtUtils.generateJwtToken(authentication);

        // When
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateJwtToken_InvalidSignature_ShouldReturnFalse() {
        // Given
        String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYzOTc0OTYwMCwiZXhwIjoxNjM5ODM2MDAwfQ.invalid_signature";

        // When
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateJwtToken_MalformedToken_ShouldReturnFalse() {
        // Given
        String malformedToken = "not.a.valid.jwt.token";

        // When
        boolean isValid = jwtUtils.validateJwtToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateJwtToken_ExpiredToken_ShouldReturnFalse() {
        // Given - Create an expired token manually
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(Instant.now().minusSeconds(3600).toEpochMilli()))
                .setExpiration(new Date(Instant.now().minusSeconds(1800).toEpochMilli())) // Expired 30 minutes ago
                .signWith(key)
                .compact();

        // When
        boolean isValid = jwtUtils.validateJwtToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateJwtToken_UnsupportedToken_ShouldReturnFalse() {
        // Given - Create an unsupported token (without signature)
        String unsupportedToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .compact(); // No signing

        // When
        boolean isValid = jwtUtils.validateJwtToken(unsupportedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateJwtToken_EmptyClaimsString_ShouldReturnFalse() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtUtils.validateJwtToken(emptyToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateJwtToken_NullToken_ShouldReturnFalse() {
        // Given
        String nullToken = null;

        // When
        boolean isValid = jwtUtils.validateJwtToken(nullToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void getUserNameFromJwtToken_ExpiredToken_ShouldThrowException() {
        // Given - Create an expired token
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(Instant.now().minusSeconds(3600).toEpochMilli()))
                .setExpiration(new Date(Instant.now().minusSeconds(1800).toEpochMilli()))
                .signWith(key)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtUtils.getUserNameFromJwtToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void getUserNameFromJwtToken_MalformedToken_ShouldThrowException() {
        // Given
        String malformedToken = "not.a.valid.jwt";

        // When & Then
        assertThatThrownBy(() -> jwtUtils.getUserNameFromJwtToken(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void generateJwtToken_TokenHasCorrectClaims() {
        // Given
        List<SimpleGrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        UserPrincipal userPrincipal = new UserPrincipal(1L, "admin", "admin@example.com", 
                "encodedpassword", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, authorities);

        // When
        String token = jwtUtils.generateJwtToken(authentication);

        // Then
        String username = jwtUtils.getUserNameFromJwtToken(token);
        assertThat(username).isEqualTo("admin");
        
        // Verify token is valid
        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
    }

    @Test
    void validateJwtToken_TokenWithDifferentSecret_ShouldReturnFalse() {
        // Given - Create token with different secret
        String differentSecret = "differentSecretKeyThatIsAlsoLongEnoughForJWTHMACAlgorithmAndMeetsThe256BitRequirement";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes());
        String tokenWithDifferentSecret = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(differentKey)
                .compact();

        // When
        boolean isValid = jwtUtils.validateJwtToken(tokenWithDifferentSecret);

        // Then
        assertThat(isValid).isFalse();
    }
}