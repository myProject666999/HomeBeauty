package com.homebeauty.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("artisan")
public class Artisan extends BaseEntity {

    @TableField("phone")
    private String phone;

    @TableField("password")
    private String password;

    @TableField("real_name")
    private String realName;

    @TableField("id_card")
    private String idCard;

    @TableField("avatar")
    private String avatar;

    @TableField("gender")
    private Integer gender;

    @TableField("age")
    private Integer age;

    @TableField("certificate_no")
    private String certificateNo;

    @TableField("certificate_img")
    private String certificateImg;

    @TableField("work_years")
    private Integer workYears;

    @TableField("skill_desc")
    private String skillDesc;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("service_radius")
    private Integer serviceRadius;

    @TableField("work_status")
    private Integer workStatus;

    @TableField("audit_status")
    private Integer auditStatus;

    @TableField("audit_remark")
    private String auditRemark;

    @TableField("rating")
    private BigDecimal rating;

    @TableField("order_count")
    private Integer orderCount;

    @TableField("status")
    private Integer status;
}
