package com.example.befindingjob.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class Register_ResetPwdRequest {
    private String email;
    private String password;
}
