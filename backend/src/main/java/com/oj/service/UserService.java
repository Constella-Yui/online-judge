package com.oj.service;

public interface UserService{
    /**
     * 注册用户
     * 描述抛异常
     */
    void register(String username,String password,String nickname);
}