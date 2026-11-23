package com.oj.service;

public interface UserService{
    /**
     * 注册用户
     * @return true成功；false用户名已存在
     */
    boolean register(String username,String password,String nickname);
}