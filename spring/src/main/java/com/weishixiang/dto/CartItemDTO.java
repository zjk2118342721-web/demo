package com.weishixiang.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemDTO {

    @NotNull(message = "请选择菜品")
    private Long dishId;

    @NotNull(message = "请选择数量")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}
