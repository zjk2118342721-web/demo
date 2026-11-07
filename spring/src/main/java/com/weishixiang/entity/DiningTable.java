package com.weishixiang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dining_table")
public class DiningTable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String tableNo;
    private Integer seats;
    private Integer status;
    private LocalDateTime createTime;
}
