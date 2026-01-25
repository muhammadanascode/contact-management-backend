package com.internship.contact_management_backend.controller;

import com.internship.contact_management_backend.dto.UserResponseDto;
import com.internship.contact_management_backend.entity.User;
import com.internship.contact_management_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public ResponseEntity<UserResponseDto> getProfileInfo() {
        //extract the email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        //get profile info
        User profileInfo = userService.findByEmail(userEmail);
        return ResponseEntity.ok(profileInfo.toDto());
    }
}
