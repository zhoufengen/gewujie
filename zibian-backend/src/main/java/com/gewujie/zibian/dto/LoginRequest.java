package com.gewujie.zibian.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String phone;
    private String code;
}
