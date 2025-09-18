package com.mailSaviour30.controller;

import com.mailSaviour30.constants.Constant;
import com.mailSaviour30.models.EmailRequest;
import com.mailSaviour30.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping(Constant.MAIL_PATH)
//    public ResponseEntity<String> sendEmail(@Valid @RequestBody EmailRequest emailRequest, BindingResult result) {
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest emailRequest, BindingResult result) {
        return ResponseEntity.ok(emailService.sendEmail(emailRequest, result));
    }

    @PostMapping(Constant.GREET_PATH)
    public  ResponseEntity<String> showGreetings(@RequestParam(Constant.SESSION_ID) String sessionId) {
        return emailService.showGreetings(sessionId);
    }
}