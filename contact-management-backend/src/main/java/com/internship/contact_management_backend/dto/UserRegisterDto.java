package com.internship.contact_management_backend.dto;

import com.internship.contact_management_backend.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserRegisterDto {
    // First name – validation
    @Size(min = 3, max = 50, message = "First name must be between 3 till 50 characters")
    @NotBlank
    private String firstName;

    // Last name – validation
    @Size(min = 3, max = 50, message = "Last name must be between 3 till 50 characters")
    @NotBlank
    private String lastName;

    // Email – unique + valid format
    @Email(message = "Email should be valid")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",message = "Email format is invalid")
    @NotBlank
    private String email;

    // Password – never expose plain text
    @Size(min = 8, message = "Password must be at least 8 characters")
    @NotBlank
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
