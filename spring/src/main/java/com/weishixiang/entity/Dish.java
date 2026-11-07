package com.weishixiang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer sales;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String categoryName;
}
