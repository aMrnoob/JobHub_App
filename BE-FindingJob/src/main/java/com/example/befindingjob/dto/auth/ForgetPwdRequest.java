package com.example.befindingjob.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForgetPwdRequest {
    private String email;
}
