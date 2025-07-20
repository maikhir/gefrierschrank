package com.gefrierschrank.app.repository;

import com.gefrierschrank.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = com.gefrierschrank.app.entity.User$Role.ADMIN")
    long countAdmins();
}