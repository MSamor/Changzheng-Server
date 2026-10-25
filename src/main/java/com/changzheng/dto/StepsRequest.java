package com.changzheng.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class StepsRequest {
    @NotBlank private String encryptedData;
    @NotBlank private String iv;
    @NotBlank private String code;
}
