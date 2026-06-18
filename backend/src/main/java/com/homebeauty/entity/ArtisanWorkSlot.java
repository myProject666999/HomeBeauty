package com.homebeauty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("artisan_work_slot")
public class ArtisanWorkSlot {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("artisan_id")
    private Long artisanId;

    @TableField("week_day")
    private Integer weekDay;

    @TableField("start_time")
    private String startTime;

    @TableField("end_time")
    private String endTime;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;
}
