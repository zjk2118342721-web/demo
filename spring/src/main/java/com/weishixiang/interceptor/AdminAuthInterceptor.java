package com.weishixiang.interceptor;

import com.weishixiang.common.BusinessException;
import com.weishixiang.common.UserContext;
import com.weishixiang.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = TokenHelper.extract(request);
        if (token == null || token.isBlank()) {
            throw new BusinessException(401, "请先登录管理后台");
        }
        try {
            Claims claims = jwtUtil.parse(token);
            if (!"admin".equals(claims.get("type", String.class))) {
                throw new BusinessException(401, "管理员登录状态无效");
            }
            UserContext.setAdminId(Long.valueOf(claims.getSubject()));
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
