package com.studyroom.studyroomreserve.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException{
    private Integer code;
    public BizException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}

