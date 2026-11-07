package com.weishixiang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cart")
public class Cart {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long dishId;
    private Integer quantity;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String dishName;
    @TableField(exist = false)
    private String dishImage;
    @TableField(exist = false)
    private BigDecimal price;
    @TableField(exist = false)
    private Integer dishStatus;
}
