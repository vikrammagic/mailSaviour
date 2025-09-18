package com.mailSaviour30.service;

import com.mailSaviour30.models.UserRequest;
import com.mailSaviour30.models.rdo.LoginResponse;

public interface LoginService {

    String showIntro();

    LoginResponse customLogin(UserRequest userRequest);

    String customlogout(String sessionId);

    public String customHostGiver();

}
