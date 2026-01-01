package com.internship.contact_management_backend.entities;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // First name – validation + security practice
    @Column(nullable = false, length = 50)
    @Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
    private String firstName;

    // Last name – validation + security practice
    @Column(nullable = false, length = 50)
    @Size(min = 3, max = 50, message = "Last name must be between 3 and 50 characters")
    private String lastName;

    // Email – unique + valid format
    @Column(nullable = false, unique = true, length = 100)
    @Email(message = "Email should be valid")
    private String email;

    // Password – never expose plain text
    @Column(nullable = false)
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // Role – use Enum for scalability
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    // Optional: Audit fields (best practice)
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    // Enum for roles – scalable for future roles
    public enum Role {
        USER,
        ADMIN
    }
}
