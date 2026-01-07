package com.internship.contact_management_backend.dto;

import com.internship.contact_management_backend.entity.Contact;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactDto {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be at least 3 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be 10-15 digits")
    private String phoneNumber;

    // Convert DTO to Entity
    public Contact toEntity() {
        return Contact.builder()
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Convert Entity to DTO
    public static ContactDto from (Contact contact){
        return ContactDto.builder()
                .name(contact.getName())
                .phoneNumber(contact.getPhoneNumber())
                .build();
    }
}
