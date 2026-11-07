package com.weishixiang.common;

public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> ADMIN_ID = new ThreadLocal<>();

    public static void setUserId(Long id) {
        USER_ID.set(id);
    }

    public static Long getUserId() {
        Long id = USER_ID.get();
        if (id == null) {
            throw new BusinessException(401, "请先登录");
        }
        return id;
    }

    public static void setAdminId(Long id) {
        ADMIN_ID.set(id);
    }

    public static Long getAdminId() {
        Long id = ADMIN_ID.get();
        if (id == null) {
            throw new BusinessException(401, "请先登录");
        }
        return id;
    }

    public static void clear() {
        USER_ID.remove();
        ADMIN_ID.remove();
    }
}
