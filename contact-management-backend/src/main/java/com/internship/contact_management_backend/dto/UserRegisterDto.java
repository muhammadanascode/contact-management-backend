package com.internship.contact_management_backend.dto;

import com.internship.contact_management_backend.entity.User;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserRegisterDto {
    // First name – validation
    @Column(nullable = false, length = 50)
    @Size(min = 3, max = 50, message = "First name must be between 3 till 50 characters")
    private String firstName;

    // Last name – validation
    @Column(nullable = false, length = 50)
    @Size(min = 3, max = 50, message = "Last name must be between 3 till 50 characters")
    private String lastName;

    // Email – unique + valid format
    @Column(nullable = false, unique = true, length = 100)
    @Email(message = "Email should be valid")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",message = "Email format is invalid")
    private String email;

    // Password – never expose plain text
    @Column(nullable = false)
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public User toEntity(){
        return User.builder()
                .firstName(this.firstName)
                .lastName(this.lastName)
                .email(this.email)
                .password(this.password)
                .build();
    }

}
