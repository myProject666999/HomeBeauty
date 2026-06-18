package com.homebeauty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("artisan_skill")
public class ArtisanSkill {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("artisan_id")
    private Long artisanId;

    @TableField("service_item_id")
    private Long serviceItemId;

    @TableField("price")
    private BigDecimal price;

    @TableField("duration")
    private Integer duration;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;
}
