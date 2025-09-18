package com.mailSaviour30.service;

import org.springframework.http.ResponseEntity;

public interface IpService {
    String getIP();

    String getIPWithVer();
}
