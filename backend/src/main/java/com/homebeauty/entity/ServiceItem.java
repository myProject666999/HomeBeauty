package com.homebeauty.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("service_item")
public class ServiceItem extends BaseEntity {

    @TableField("category_id")
    private Long categoryId;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("cover_img")
    private String coverImg;

    @TableField("base_price")
    private BigDecimal basePrice;

    @TableField("default_duration")
    private Integer defaultDuration;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private Integer status;
}
