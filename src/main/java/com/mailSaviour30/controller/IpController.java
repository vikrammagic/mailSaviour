package com.mailSaviour30.controller;


import com.mailSaviour30.constants.Constant;
import com.mailSaviour30.service.IpService;
import com.mailSaviour30.service.impl.IpServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IpController {

    @Autowired
    private IpService ipService;

    @GetMapping(Constant.IP_PATH)
    public ResponseEntity<String> getIP(){
        return ResponseEntity.ok(ipService.getIP());
    }

    @GetMapping(Constant.IP_VER_PATH)
    public ResponseEntity<String> getIPWithVer(){
        return ResponseEntity.ok(ipService.getIPWithVer());
    }
}