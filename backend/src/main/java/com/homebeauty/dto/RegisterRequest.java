package com.homebeauty.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String phone;

    private String password;

    private String nickname;

    private String realName;

    private String idCard;

    private String role;
}
