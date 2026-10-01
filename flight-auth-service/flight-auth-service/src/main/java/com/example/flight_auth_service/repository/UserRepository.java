package com.example.flight_auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.flight_auth_service.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}