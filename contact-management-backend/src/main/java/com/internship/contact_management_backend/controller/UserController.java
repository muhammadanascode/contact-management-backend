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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService,
                         AuthenticationManager authenticationManager,
                         UserDetailsServiceImpl userDetailsService,
                         JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegisterDto user) {
        User savedUser = userService.register(user.toEntity());
        return ResponseEntity
                .status(HttpStatus.CREATED)  // <-- returns 201 instead of 200
                .body(savedUser.toDto());
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
