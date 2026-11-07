package com.weishixiang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long userId;
    private Integer rating;
    private String content;
    private String images;
    private Integer anonymous;
    private String reply;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String userNickname;
    @TableField(exist = false)
    private String userAvatar;
    @TableField(exist = false)
    private String orderNo;
    @TableField(exist = false)
    private String dishNames;
}
