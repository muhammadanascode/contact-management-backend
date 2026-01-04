package com.internship.contact_management_backend.dto;

import com.internship.contact_management_backend.entity.User;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserResponseDto {
    private String firstName;
    private String lastName;
    private String email;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                              .firstName(user.getFirstName())
                              .lastName(user.getLastName())
                              .email(user.getEmail())
                              .build();
    }
}
