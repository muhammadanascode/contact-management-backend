package com.internship.contact_management_backend.service;

import com.internship.contact_management_backend.dto.UpdatePasswordDto;
import com.internship.contact_management_backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.internship.contact_management_backend.entity.User;

@Service
@Slf4j
public class UserService {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        // Register a new user
        public User register(User user) {

            //1. Email should not be null or empty
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email cannot be null or empty");
            }

            // 2. Validate business rule: email must be unique
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }

            // 3. Hash password before saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // 4. Save user
            User saved =  userRepository.save(user);
            log.info("User registered successfully id={} email={}", saved.getId(), saved.getEmail());
            return  saved;
        }

        public User findByEmail(String email) {
            return userRepository.findByEmail(email).orElse(null);
    }

    public void updatePassword(String email, UpdatePasswordDto dto) {
        // Validate passwords match
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        //throw error if the new password is null
        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        User user = findByEmail(email);

        //if the user doesn't exists
        if (user == null) {
            throw new NullPointerException("User not found");
        }

        // Validate old password
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        // Update entity
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Password updated successfully for user={}", email);
    }

}
