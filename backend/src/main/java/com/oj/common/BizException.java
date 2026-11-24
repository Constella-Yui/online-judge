package com.oj.common;

import lombok.Getter;

/**
 * 业务异常：Service层发现“业务上不成立“时抛出
 * 由GlobalExceptionHandler统一翻译Result返回前端
 */
@Getter
public class BizException extends RuntimeException{

    private final ResultCode resultCode;

    public BizException(ResultCode resultCode){
        super(resultCode.getMessage());
        this.resultCode=resultCode;
    }
    public BizException(ResultCode resultCode,String message){
        super(message);
        this.resultCode=resultCode;
    }
}
