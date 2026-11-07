package com.weishixiang.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReviewDTO {

    @NotNull(message = "请选择订单")
    private Long orderId;

    @NotNull(message = "请选择评分")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer rating;

    private String content;
    private List<String> images;
    private Integer anonymous;
}
