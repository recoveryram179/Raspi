package com.hawks.raspi.helpers;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpAddress {
    public static String getIpAddress() {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return inetAddress.getHostAddress();
    }

    public static String getBaseUrl() {
        return "https://" + getIpAddress() + ":" + "8080" + "/";
    }

    public static String getDomain(HttpServletRequest request) {
        String scheme = request.getScheme();         // http or https
        String domain = request.getServerName();     // localhost or yourdomain.com
        int port = request.getServerPort();          // 8080 or 443

        String baseUrl = scheme + "://" + domain + ":" + port + "/";
        if (domain.contains("cloudflare")) {
            baseUrl = "https://" + domain + "/";
        }
        System.out.println("Running on: " + baseUrl);
        return baseUrl;
    }

    public static String getWebSocket(HttpServletRequest request) {
        String domain = request.getServerName();     // localhost or yourdomain.com
        int port = request.getServerPort();          // 8080 or 443

        String baseUrl = domain + ":" + port;
        if (domain.contains("cloudflare")) {
            baseUrl = domain;
        }
        System.out.println("Running on: " + baseUrl);
        return baseUrl;
    }
}
