package com.oj.controller;

import com.oj.common.Result;
import com.oj.common.ResultCode;
import com.oj.dto.RegisterRequest;
import com.oj.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest req){
        userService.register(req.getUsername(),req.getPassword(),req.getNickname());
        return Result.success();
    }
}
