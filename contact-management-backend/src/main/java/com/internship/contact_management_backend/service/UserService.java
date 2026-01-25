package com.internship.contact_management_backend.service;

import com.internship.contact_management_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.internship.contact_management_backend.entity.User;

@Service
public class UserService {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        // Register a new user
        public User register(User user) {

            // 1. Validate business rule: email must be unique
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }

            // 2. Hash password before saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // 3. Save user
            return userRepository.save(user);
        }

        public User findByEmail(String email) {
            return userRepository.findByEmail(email).orElse(null);
    }
}
