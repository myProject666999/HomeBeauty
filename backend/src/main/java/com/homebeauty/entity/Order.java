package com.homebeauty.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`order`")
public class Order extends BaseEntity {

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("artisan_id")
    private Long artisanId;

    @TableField("service_item_id")
    private Long serviceItemId;

    @TableField("service_name")
    private String serviceName;

    @TableField("price")
    private BigDecimal price;

    @TableField("duration")
    private Integer duration;

    @TableField("appointment_date")
    private LocalDate appointmentDate;

    @TableField("appointment_time")
    private String appointmentTime;

    @TableField("address")
    private String address;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    @TableField("remark")
    private String remark;

    @TableField("order_status")
    private Integer orderStatus;

    @TableField("pay_status")
    private Integer payStatus;

    @TableField("pay_amount")
    private BigDecimal payAmount;

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField("check_in_code")
    private String checkInCode;

    @TableField("check_in_time")
    private LocalDateTime checkInTime;

    @TableField("start_service_time")
    private LocalDateTime startServiceTime;

    @TableField("end_service_time")
    private LocalDateTime endServiceTime;

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    @TableField("dispatch_time")
    private LocalDateTime dispatchTime;

    @TableField("accept_time")
    private LocalDateTime acceptTime;
}
