package com.example.campusstudyroombookingsystem.model;

/**
 * 登录请求体，对应后端 LoginVo
 */
public class LoginRequest {
    private final String username;
    private final String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
