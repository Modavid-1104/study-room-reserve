package com.studyroom.studyroomreserve.exception;

import com.studyroom.studyroomreserve.util.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleAllException(Exception e) {
        e.printStackTrace();
        return Result.fail(500, "服务器内部错误");
    }
}

