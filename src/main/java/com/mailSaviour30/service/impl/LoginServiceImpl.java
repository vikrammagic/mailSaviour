package com.mailSaviour30.service.impl;

import com.mailSaviour30.constants.Constant;
import com.mailSaviour30.entities.UserEntity;
import com.mailSaviour30.models.UserRequest;
import com.mailSaviour30.models.rdo.LoginResponse;
import com.mailSaviour30.repositories.UserRepository;
import com.mailSaviour30.service.LoginService;
import com.mailSaviour30.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserRepository uRepository;
    @Autowired
    private MessageService messageService;


    @Override
    public String showIntro() {
        return messageService.getMessageContentByName(Constant.INTRO);
    }

    @Override
    public LoginResponse customLogin(UserRequest userRequest) {
        HttpStatus status = HttpStatus.valueOf(loginUser(userRequest.getUserName(), userRequest.getPassword()));
        String sessionId = null;
        String redirectUrl = null;
        String message = null;

        switch (status) {
            case OK:
                UserEntity user = getUserEntityByUsername(userRequest.getUserName());
                sessionId = user.getSessionId();
                redirectUrl = Constant.MAIL_ENGINE_URL;
                break;
            case NOT_EXTENDED:
                message = messageService.getMessageContentByName(Constant.EXPIRED_USER);
                break;
            case UNAUTHORIZED:
                message = messageService.getMessageContentByName(Constant.UNAUTHORIZED_USER);
                break;
            default:
                message = "An unexpected error occurred.";
                log.info("Errored user is: {}", userRequest.getUserName());
                status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new LoginResponse(status.value(), sessionId, redirectUrl, message);
    }


    @Override
    public String customlogout(String sessionId) {
        try {
            logoutUser(sessionId);
            return Constant.LOGIN_URL;
        } catch (Exception e) {
            return Constant.LOGIN_URL;
        }
    }

    @Override
    public String customHostGiver() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            // Get the hostname
            return request.getServerName();
        } catch (Exception e) {
//            log.error("e: ", e);
            return e.getMessage();
        }
    }


    //helper methods
    public UserEntity getUserEntityByUsername(String username) {
        return uRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

     public int loginUser(String username, String password) {
         try {
             UserEntity user = getUserEntityByUsername(username);
             boolean isExpired = user.getValidUntil().isBefore(LocalDate.now());
             if (Objects.equals(password, user.getPassword())
                     && Objects.equals(getServiceHost(), user.getHost())
                     //request context holder (host details) and then change the DB to only accept the host details
                     && !isExpired
                     && user.getIsAssigned()) {
                     String sessionId = UUID.randomUUID().toString();
                     user.setSessionId(sessionId);
                     uRepository.save(user);
                     return HttpStatus.OK.value();
             } else if (isExpired){
                 return HttpStatus.NOT_EXTENDED.value();
             } else {
                 return HttpStatus.UNAUTHORIZED.value();
             }
         } catch (Exception ignored) {
         }
         return HttpStatus.UNAUTHORIZED.value();
     }


    public void logoutUser(String sessionId) {
        UserEntity user = uRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        user.setSessionId(Constant.DEFAULT_SESSION_ID);
        uRepository.save(user);
    }

    public String getServiceHost(){
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            // Get the hostname
            return request.getServerName();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
//            return ResponseEntity.ok(e.getMessage());
        }
    }
}