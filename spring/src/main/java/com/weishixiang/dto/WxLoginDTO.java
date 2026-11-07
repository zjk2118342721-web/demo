package com.weishixiang.dto;

import lombok.Data;

@Data
public class WxLoginDTO {
    /** 微信登录 code；本地模拟模式下作为稳定的设备标识 */
    private String code;
    private String nickname;
    private String avatar;
}
