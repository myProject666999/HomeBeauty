package com.homebeauty.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateOrderRequest {

    private Long userId;

    private Long serviceItemId;

    private Long artisanId;

    private LocalDate appointmentDate;

    private String appointmentTime;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String contactName;

    private String contactPhone;

    private String remark;
}
