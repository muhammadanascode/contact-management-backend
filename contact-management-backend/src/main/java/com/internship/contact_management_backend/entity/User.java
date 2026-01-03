package com.internship.contact_management_backend.entity;
import com.internship.contact_management_backend.dto.UserResponseDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    // Optional: Audit fields (best practice)
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public UserResponseDto toDto() {
        return UserResponseDto.from(this);
    }

}
