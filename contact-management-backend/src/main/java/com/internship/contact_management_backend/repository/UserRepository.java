package com.internship.contact_management_backend.repository;

import com.internship.contact_management_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    // Find user by email
    Optional<User>  findByEmail(String email);

    // Check if email already exists
    boolean existsByEmail(String email);

}
