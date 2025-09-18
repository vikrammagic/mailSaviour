package com.mailSaviour30.service;

import com.mailSaviour30.models.EmailRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public interface EmailService {

     String sendEmail(EmailRequest emailRequestDto, BindingResult result);

     ResponseEntity<String> showGreetings(String sessionId);
}
