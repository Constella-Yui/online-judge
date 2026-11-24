package com.oj.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：Controller链路抛出所有异常都在这里处理为Result
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /*1.业务失败（Service主动throw;*/
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e){
        return Result.fail(e.getResultCode(),e.getMessage());
    }
    /*2.@Valid校验未通过，取第一条错误信息*/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e){
        FieldError first = e.getBindingResult().getFieldError();
        String msg=(first==null)?ResultCode.PARAM_ERROR.getMessage():first.getDefaultMessage();
        return Result.fail(ResultCode.PARAM_ERROR,msg);
    }
    //补充：请求体不是合法JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void>handlerUnreadable(HttpMessageNotReadableException e){
        log.warn("请求体解析失败：{}",e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR,"请求体格式错误");
    }
    /*3.兜底*/
    @ExceptionHandler(Exception.class)
    public Result<Void>handlerOthers(Exception e){
        log.error("未捕获异常",e);
        return Result.fail(ResultCode.ERROR);
    }
}
