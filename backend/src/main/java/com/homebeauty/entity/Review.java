package com.homebeauty.entity;

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

    @TableField("order_id")
    private Long orderId;

    @TableField("user_id")
    private Long userId;

    @TableField("artisan_id")
    private Long artisanId;

    @TableField("rating")
    private Integer rating;

    @TableField("content")
    private String content;

    @TableField("imgs")
    private String imgs;

    @TableField("create_time")
    private LocalDateTime createTime;
}
