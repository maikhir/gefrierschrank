package com.gefrierschrank.app.service;

import com.gefrierschrank.app.dto.UserDto;
import com.gefrierschrank.app.entity.User;
import com.gefrierschrank.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDto createUser(String username, String email, String password, User.Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User(username, email, passwordEncoder.encode(password), role);
        user = userRepository.save(user);
        
        logger.info("Created new user: {} with role: {}", username, role);
        return new UserDto(user);
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserDto::new);
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id)
                .map(UserDto::new);
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (userDto.getUsername() != null && !userDto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(userDto.getUsername())) {
                throw new RuntimeException("Error: Username is already taken!");
            }
            user.setUsername(userDto.getUsername());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new RuntimeException("Error: Email is already in use!");
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getRole() != null) {
            user.setRole(userDto.getRole());
        }

        if (userDto.getNotificationsEnabled() != null) {
            user.setNotificationsEnabled(userDto.getNotificationsEnabled());
        }

        user = userRepository.save(user);
        logger.info("Updated user: {}", user.getUsername());
        return new UserDto(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Prevent deletion of the last admin
        if (user.getRole() == User.Role.ADMIN) {
            long adminCount = userRepository.countAdmins();
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete the last admin user");
            }
        }

        userRepository.delete(user);
        logger.info("Deleted user: {}", user.getUsername());
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}