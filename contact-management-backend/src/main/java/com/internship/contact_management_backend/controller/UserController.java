package com.internship.contact_management_backend.controller;

import com.internship.contact_management_backend.dto.LoginResponseDto;
import com.internship.contact_management_backend.dto.UserLoginDto;
import com.internship.contact_management_backend.dto.UserRegisterDto;
import com.internship.contact_management_backend.dto.UserResponseDto;
import com.internship.contact_management_backend.entity.User;
import com.internship.contact_management_backend.service.UserDetailsServiceImpl;
import com.internship.contact_management_backend.service.UserService;
import com.internship.contact_management_backend.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegisterDto user) {
        User savedUser = userService.register(user.toEntity());
        return ResponseEntity.ok(savedUser.toDto());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody UserLoginDto user){

        //Authenticate user
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPassword()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        //Generate JWT token
        String token = jwtUtil.generateToken(
                userDetails.getUsername());

        //Convert raw token into response dto
        LoginResponseDto response = new LoginResponseDto(token);

        return ResponseEntity.ok(response);
    }

}
