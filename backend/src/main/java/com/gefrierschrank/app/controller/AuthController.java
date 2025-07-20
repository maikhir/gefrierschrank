package com.gefrierschrank.app.controller;

import com.gefrierschrank.app.dto.LoginRequest;
import com.gefrierschrank.app.dto.LoginResponse;
import com.gefrierschrank.app.dto.MessageResponse;
import com.gefrierschrank.app.dto.UserDto;
import com.gefrierschrank.app.security.JwtUtils;
import com.gefrierschrank.app.security.UserPrincipal;
import com.gefrierschrank.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            UserDto userDto = userService.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(new LoginResponse(jwt, userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: Invalid username or password!"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: No authentication found!"));
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserDto userDto = userService.findByUsername(userPrincipal.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error: No authentication found!"));
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userPrincipal.getUsername());
        
        UserDto userDto = userService.findByUsername(userPrincipal.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new LoginResponse(jwt, userDto));
    }
}