package com.weishixiang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weishixiang.entity.User;
import com.weishixiang.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public IPage<User> pageAdmin(long page, long size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(User::getNickname, keyword)
                        .or()
                        .like(User::getPhone, keyword))
                .orderByDesc(User::getCreateTime);
        return userMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
