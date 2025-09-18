package com.mailSaviour30.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class EmailRequest {
    @NotBlank(message = "Invalid Sent From")
    @Email
    private String sentFrom;

    @NotBlank
    @Size(min = 19, max = 19)
    private String appPass;

    @NotBlank
    @Size(max = 150)
    private String subject;

//    @NotBlank
//    @Size(max = 10000)
//    private String body;

    @NotBlank
    @Size(max = 10000)
    private String realBody;

    @NotEmpty(message = "Email list cannot be empty")
//    private List<@Email String> eMails;
    private List<String> eMails;

    @NotBlank
    @Size(min = 3, max = 40)
    private String firstName;

    @NotBlank
    private Boolean isPort587;

//    @NotBlank
//    private Boolean isHTML;

    private String sessionId;
}