package com.gefrierschrank.app.config;

import com.gefrierschrank.app.entity.User;
import com.gefrierschrank.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;

@TestConfiguration
public class TestDataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // Create test admin user if not exists
        if (!userRepository.existsByUsername("testadmin")) {
            User admin = new User("testadmin", "testadmin@example.com", 
                                passwordEncoder.encode("testpass"), User.Role.ADMIN);
            userRepository.save(admin);
        }
    }
}