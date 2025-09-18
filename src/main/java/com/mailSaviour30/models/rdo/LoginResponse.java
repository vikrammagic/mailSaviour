package com.mailSaviour30.models.rdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private int status;
    private String sessionId;
    private String redirectUrl;
    private String message;
}