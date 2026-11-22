package com.example.campusstudyroombookingsystem.model;

/**
 * 对应后端 TokenVo：登录成功后 data 里的对象
 * { "token":"xxxxx", "role":1 }
 */
public class TokenResponse {
    private String token;
    private Integer role;

    public String getToken() {
        return token;
    }

    public Integer getRole() {
        return role;
    }
}
