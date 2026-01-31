package com.internship.contact_management_backend.dto;

import com.internship.contact_management_backend.entity.Contact;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactDto {

    private Long id;

    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 50, message = "First name must be 3–100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 50, message = "Last name must be 3–100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Email label is required")
    @Size(min = 3, max = 20, message = "Email label must be 3–20 characters")
    private String emailLabel;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+[1-9][0-9]{1,14}$",
            message = "Phone number must be in international format (E.164)"
    )
    private String phoneNumber;

    @NotBlank(message = "Phone number label is required")
    @Size(min = 3, max = 20, message = "Phone label must be 3–20 characters")
    private String phoneNumberLabel;

    // DTO → Entity
    public Contact toEntity() {
        return Contact.builder()
                      .id(id)
                      .firstName(firstName)
                      .lastName(lastName)
                      .email(email)
                      .emailLabel(emailLabel)
                      .phoneNumber(phoneNumber)
                      .phoneNumberLabel(phoneNumberLabel)
                      .createdAt(LocalDateTime.now())
                      .updatedAt(LocalDateTime.now())
                      .build();
    }

    // Entity → DTO
    public static ContactDto from(Contact contact) {
        return ContactDto.builder()
                         .id(contact.getId())
                         .firstName(contact.getFirstName())
                         .lastName(contact.getLastName())
                         .email(contact.getEmail())
                         .emailLabel(contact.getEmailLabel())
                         .phoneNumber(contact.getPhoneNumber())
                         .phoneNumberLabel(contact.getPhoneNumberLabel())
                         .build();
    }
}
