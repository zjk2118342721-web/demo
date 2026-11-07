package com.weishixiang.common;

import lombok.Data;

@Data
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> ok() {
        return build(200, "操作成功", null);
    }

    public static <T> Result<T> ok(T data) {
        return build(200, "操作成功", data);
    }

    public static <T> Result<T> ok(String msg, T data) {
        return build(200, msg, data);
    }

    public static <T> Result<T> error(String msg) {
        return build(500, msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return build(code, msg, null);
    }

    private static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
