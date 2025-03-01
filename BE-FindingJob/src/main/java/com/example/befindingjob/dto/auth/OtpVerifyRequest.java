package com.example.befindingjob.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpVerifyRequest {
    private String email;
    private String otp;
}
