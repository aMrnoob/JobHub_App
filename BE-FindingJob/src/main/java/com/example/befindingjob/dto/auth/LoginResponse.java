package com.example.befindingjob.dto.auth;

import com.example.befindingjob.entity.enumm.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private int userId;
    private Role role;
    private String fullName;
}
