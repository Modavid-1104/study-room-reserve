package com.example.campusstudyroombookingsystem.model;

/**
 * 注册请求体，对应后端 RegisterVo
 */
public class RegisterRequest {
    private final String username;
    private final String password;
    private final String nickname;
    private final String phone;

    public RegisterRequest(String username, String password, String nickname, String phone) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.phone = phone;
    }
}
