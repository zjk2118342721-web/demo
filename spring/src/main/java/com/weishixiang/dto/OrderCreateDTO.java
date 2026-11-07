package com.weishixiang.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {

    @NotEmpty(message = "请至少选择一个菜品")
    private List<CartItemDTO> items;

    private String tableNo;
    private String remark;
}
