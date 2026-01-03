package com.internship.contact_management_backend.controller;

import com.internship.contact_management_backend.dto.UserRegisterDto;
import com.internship.contact_management_backend.dto.UserResponseDto;
import com.internship.contact_management_backend.entity.User;
import com.internship.contact_management_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRegisterDto user) {
        User savedUser = userService.register(user.toEntity());
        return ResponseEntity.ok(savedUser.toDto());
    }

}
