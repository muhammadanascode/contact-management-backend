package com.internship.contact_management_backend.dto;

import com.internship.contact_management_backend.entity.User;
import lombok.Builder;

@Builder
public class UserRegisterDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public User toEntity(){
        return User.builder()
                .firstName(this.firstName)
                .lastName(this.lastName)
                .email(this.email)
                .password(this.password)
                .build();
    }

    public static UserRegisterDto from(User user) {
        return UserRegisterDto.builder()
                              .firstName(user.getFirstName())
                              .lastName(user.getLastName())
                              .email(user.getEmail())
                              .password(null)
                              .build();
    }

}
