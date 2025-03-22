package com.example.befindingjob.dto.admin;

import com.example.befindingjob.entity.enumm.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private int userId;
    private String fullName;
    private String email;
    private String password;
    private Role role;
    private String address;
    private String dateOfBirth;
    private String phone;
    private String created_at;
    private String updated_at;
}
