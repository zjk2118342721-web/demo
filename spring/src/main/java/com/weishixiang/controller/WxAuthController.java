package com.weishixiang.controller;

import com.weishixiang.common.Result;
import com.weishixiang.common.UserContext;
import com.weishixiang.dto.ProfileDTO;
import com.weishixiang.dto.WxLoginDTO;
import com.weishixiang.entity.User;
import com.weishixiang.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/wx")
@RequiredArgsConstructor
public class WxAuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody(required = false) WxLoginDTO dto) {
        return Result.ok(authService.wxLogin(dto == null ? new WxLoginDTO() : dto));
    }

    @GetMapping("/info")
    public Result<User> info() {
        return Result.ok(authService.userInfo(UserContext.getUserId()));
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody ProfileDTO dto) {
        return Result.ok(authService.updateProfile(UserContext.getUserId(), dto));
    }
}
