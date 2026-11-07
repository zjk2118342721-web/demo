package com.weishixiang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.weishixiang.common.Result;
import com.weishixiang.entity.User;
import com.weishixiang.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/page")
    public Result<IPage<User>> page(@RequestParam(defaultValue = "1") long page,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam(required = false) String keyword) {
        return Result.ok(userService.pageAdmin(page, size, keyword));
    }
}
