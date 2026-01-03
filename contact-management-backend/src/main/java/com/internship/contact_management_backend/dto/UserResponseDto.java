package com.internship.contact_management_backend.dto;

import com.internship.contact_management_backend.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                              .id(user.getId())
                              .firstName(user.getFirstName())
                              .lastName(user.getLastName())
                              .email(user.getEmail())
                              .build();
    }
}
