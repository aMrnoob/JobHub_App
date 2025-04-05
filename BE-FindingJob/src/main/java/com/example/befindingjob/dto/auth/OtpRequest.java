package com.example.befindingjob.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OtpRequest {
    private String email;
}
