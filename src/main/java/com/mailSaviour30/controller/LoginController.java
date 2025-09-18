package com.mailSaviour30.controller;

import com.mailSaviour30.constants.Constant;
import com.mailSaviour30.models.UserRequest;
import com.mailSaviour30.models.rdo.LoginResponse;
import com.mailSaviour30.service.LoginService;
import com.mailSaviour30.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private MessageService messageService;


    @GetMapping(Constant.INTRO_PATH)
    public  ResponseEntity<String> showIntro() {
        return ResponseEntity.ok(loginService.showIntro());
    }

    @PostMapping(Constant.LOGIN_PATH)
    public LoginResponse customLogin(@Valid @RequestBody UserRequest userRequest) {
        return loginService.customLogin(userRequest);
    }

    @PostMapping(Constant.LOGOUT_PATH)
    public RedirectView customlogout(@RequestParam(Constant.SESSION_ID) String sessionId) {
        return new RedirectView(loginService.customlogout(sessionId));
    }

    @GetMapping(Constant.HOST_PATH)
    public ResponseEntity<String> customHostGiver() {
        return ResponseEntity.ok(loginService.customHostGiver());
    }
}