package com.oj.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode{
    SUCCESS(200,"成功"),
    PARAM_ERROR(400,"参数错误"),
    UNAUTHORIZED(401,"未登录"),
    FORBIDDEN(403,"无权限"),
    NOT_FOUND(404,"资源不存在"),
    CONFLICT(409,"资源冲突"),
    ERROR(500,"系统错误");

    private final int code;
    private final String message;

}
