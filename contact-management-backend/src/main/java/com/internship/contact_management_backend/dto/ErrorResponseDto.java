package com.internship.contact_management_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponseDto {
    private LocalDateTime timestamp;
    private int status;        // HTTP status code
    private String error;      // Short description
    private String message;    // Detailed message
    private String path;       // Endpoint path
}
