package com.mailSaviour30.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandling {

    @ExceptionHandler({})
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return  new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
