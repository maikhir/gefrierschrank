package com.gefrierschrank.app.service;

import com.gefrierschrank.app.entity.Category;
import com.gefrierschrank.app.entity.User;
import com.gefrierschrank.app.repository.CategoryRepository;
import com.gefrierschrank.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DataInitializationService implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting data initialization...");
        
        initializeUsers();
        initializeCategories();
        
        logger.info("Data initialization completed successfully");
    }
    
    private void initializeUsers() {
        // Create admin user if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@gefrierschrank.de");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            
            userRepository.save(admin);
            logger.info("Admin user created: admin/admin123");
        }
        
        // Create regular user if it doesn't exist
        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@gefrierschrank.de");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole(User.Role.USER);
            
            userRepository.save(user);
            logger.info("Regular user created: user/user123");
        }
    }
    
    private void initializeCategories() {
        // Only create categories if none exist
        if (categoryRepository.count() == 0) {
            logger.info("Creating default product categories...");
            
            createCategory("Fleisch", "🥩", "kg", "0.1", "0.1", "50.0");
            createCategory("Fisch", "🐟", "kg", "0.1", "0.1", "20.0");
            createCategory("Gemüse", "🥕", "kg", "0.1", "0.1", "30.0");
            createCategory("Obst", "🍎", "kg", "0.1", "0.1", "25.0");
            createCategory("Brot & Backwaren", "🍞", "Stück", "1", "1", "20");
            createCategory("Milchprodukte", "🧀", "kg", "0.1", "0.1", "15.0");
            createCategory("Fertiggerichte", "🍲", "Stück", "1", "1", "50");
            createCategory("Eis", "🍦", "L", "0.1", "0.1", "10.0");
            createCategory("Tiefkühlgemüse", "🥦", "kg", "0.1", "0.1", "20.0");
            createCategory("Pizza & Teigwaren", "🍕", "Stück", "1", "1", "30");
            createCategory("Suppen", "🍜", "L", "0.1", "0.1", "5.0");
            createCategory("Sonstiges", "📦", "Stück", "1", "1", "100");
            
            logger.info("Created {} default categories", categoryRepository.count());
        } else {
            logger.info("Categories already exist, skipping initialization");
        }
    }
    
    private void createCategory(String name, String icon, String defaultUnit, 
                              String unitStep, String minValue, String maxValue) {
        Category category = new Category();
        category.setName(name);
        category.setIcon(icon);
        category.setDefaultUnit(defaultUnit);
        category.setUnitStep(new BigDecimal(unitStep));
        category.setMinValue(new BigDecimal(minValue));
        category.setMaxValue(new BigDecimal(maxValue));
        
        categoryRepository.save(category);
        logger.debug("Created category: {} with unit: {}", name, defaultUnit);
    }
}