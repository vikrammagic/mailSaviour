package com.mailSaviour30.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "Username is required.")
    @Size(min = 10, max = 10, message = "Username must be exactly 10 characters long.")
    private String userName;

    @NotBlank(message = "Password is required.")
    @Size(min = 16, max = 16, message = "Password must be exactly 16 characters long.")
    private String password;

}

