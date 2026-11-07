package com.weishixiang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weishixiang.common.BusinessException;
import com.weishixiang.dto.AdminLoginDTO;
import com.weishixiang.dto.ProfileDTO;
import com.weishixiang.dto.WxLoginDTO;
import com.weishixiang.entity.Admin;
import com.weishixiang.entity.User;
import com.weishixiang.mapper.AdminMapper;
import com.weishixiang.mapper.UserMapper;
import com.weishixiang.util.JwtUtil;
import com.weishixiang.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ObjectMapper objectMapper;

    @Value("${app.wx.appid}")
    private String appId;

    @Value("${app.wx.secret}")
    private String appSecret;

    public Map<String, Object> wxLogin(WxLoginDTO dto) {
        String openid = resolveOpenid(dto.getCode());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getOpenid, openid)
                .last("LIMIT 1"));

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : "微信用户");
            user.setAvatar(dto.getAvatar() == null ? "" : dto.getAvatar());
            user.setPhone("");
            userMapper.insert(user);
        } else {
            boolean changed = false;
            if (StringUtils.hasText(dto.getNickname()) && !dto.getNickname().equals(user.getNickname())) {
                user.setNickname(dto.getNickname());
                changed = true;
            }
            if (StringUtils.hasText(dto.getAvatar()) && !dto.getAvatar().equals(user.getAvatar())) {
                user.setAvatar(dto.getAvatar());
                changed = true;
            }
            if (changed) {
                userMapper.updateById(user);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("token", jwtUtil.generateUserToken(user.getId()));
        data.put("user", user);
        return data;
    }

    private String resolveOpenid(String code) {
        if (StringUtils.hasText(appId) && StringUtils.hasText(appSecret)) {
            return requestWechatOpenid(code);
        }
        String deviceId = StringUtils.hasText(code) ? code.replaceAll("[^a-zA-Z0-9_]", "") : "demo";
        if (!StringUtils.hasText(deviceId)) {
            deviceId = "demo";
        }
        return "mock_" + deviceId;
    }

    private String requestWechatOpenid(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("缺少微信登录 code");
        }
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId
                + "&secret=" + appSecret
                + "&js_code=" + code
                + "&grant_type=authorization_code";
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            String body = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(body);
            String openid = node.path("openid").asText();
            if (!StringUtils.hasText(openid)) {
                throw new BusinessException("微信登录失败：" + node.path("errmsg").asText("未知错误"));
            }
            return openid;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("微信登录服务暂不可用，请稍后重试");
        }
    }

    public Map<String, Object> adminLogin(AdminLoginDTO dto) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, dto.getUsername())
                .last("LIMIT 1"));
        if (admin == null || !PasswordUtil.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(400, "账号或密码错误");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("token", jwtUtil.generateAdminToken(admin.getId()));
        data.put("admin", admin);
        return data;
    }

    public Admin adminInfo(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(401, "管理员不存在");
        }
        return admin;
    }

    public User userInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return user;
    }

    public User updateProfile(Long userId, ProfileDTO dto) {
        User user = userInfo(userId);
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        userMapper.updateById(user);
        return user;
    }
}
