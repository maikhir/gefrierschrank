package com.gefrierschrank.app.config;

import com.gefrierschrank.app.entity.User;
import com.gefrierschrank.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        // Create default admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "admin@gefrierschrank.app", 
                                passwordEncoder.encode("admin123"), User.Role.ADMIN);
            userRepository.save(admin);
            logger.info("Created default admin user: admin / admin123");
        }

        // Create default regular user if not exists  
        if (!userRepository.existsByUsername("user")) {
            User user = new User("user", "user@gefrierschrank.app", 
                                passwordEncoder.encode("user123"), User.Role.USER);
            userRepository.save(user);
            logger.info("Created default user: user / user123");
        }

        logger.info("Database initialization completed");
    }
}