package com.mailSaviour30.service.impl;

import com.mailSaviour30.service.IpService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@Service
public class IpServiceImpl implements IpService {

    @Override
    public String getIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String getIPWithVer() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isLinkLocalAddress() || address.isLoopbackAddress()) {
                        continue;
                    }
                    if (address.getAddress().length == 16) { // Check for IPv6 length (16 bytes)
                        System.out.println("IPv6 Address: " + address.getHostAddress());
                        return ("IPv6 Address: " + address.getHostAddress());
                    } else if (address.getAddress().length == 4) { // Check for IPv4 length (4 bytes)
                        System.out.println("IPv4 Address: " + address.getHostAddress());
                        return ("IPv4 Address: " + address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "Failed to get IP";
    }
}
