package com.weishixiang.controller;

import com.weishixiang.common.Result;
import com.weishixiang.common.UserContext;
import com.weishixiang.dto.AdminLoginDTO;
import com.weishixiang.entity.Admin;
import com.weishixiang.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginDTO dto) {
        return Result.ok("登录成功", authService.adminLogin(dto));
    }

    @GetMapping("/info")
    public Result<Admin> info() {
        return Result.ok(authService.adminInfo(UserContext.getAdminId()));
    }
}
