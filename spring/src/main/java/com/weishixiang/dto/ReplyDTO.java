package com.weishixiang.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyDTO {

    @NotBlank(message = "请输入回复内容")
    private String reply;
}
