package com.weishixiang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private String tableNo;
    private BigDecimal totalAmount;
    /**
     * 0待付款 1待接单 2制作中 3待上菜 4已完成 5已取消
     */
    private Integer status;
    private String remark;
    private LocalDateTime payTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String userNickname;
    @TableField(exist = false)
    private List<OrderItem> items;
    @TableField(exist = false)
    private Boolean reviewed;
    @TableField(exist = false)
    private String reviewContent;
    @TableField(exist = false)
    private Integer reviewRating;
}
