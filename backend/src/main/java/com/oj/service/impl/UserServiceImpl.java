package com.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oj.common.BizException;
import com.oj.common.ResultCode;
import com.oj.entity.User;
import com.oj.mapper.UserMapper;
import com.oj.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();

    @Override
    public void register(String username,String rawPassword,String nickname){
        //1.查重
        Long count=userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername,username));
        if(count>0){
            throw new BizException(ResultCode.CONFLICT,"用户名已存在");
        }
        //2.组装实体，密码哈希
        User user=new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);
        //3.数据库防线
        try{
            userMapper.insert(user);
        }catch(DuplicateKeyException e){
            throw new BizException(ResultCode.CONFLICT,"用户名已存在");
        }
    }
}