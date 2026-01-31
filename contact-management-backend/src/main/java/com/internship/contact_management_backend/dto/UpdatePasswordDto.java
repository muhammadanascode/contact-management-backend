package com.internship.contact_management_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdatePasswordDto {
    // Old Password
    @NotBlank(message = "Old password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String oldPassword;

    // New Password
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;

    // Confirm new Password
    @NotBlank(message = "Confirm password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String confirmPassword;
}
