package com.example.campusstudyroombookingsystem.model;

/**
 * 对应后端统一返回结果 Result<T>：
 * { "code":200, "msg":"操作成功", "data":... }
 */
public class ApiResponse<T> {
    private Integer code;
    private String msg;
    private T data;

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    /** 业务是否成功：以后端 code==200 为准，而不是 HTTP 状态码 */
    public boolean isSuccess() {
        return code != null && code == 200;
    }
}
