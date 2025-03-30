package com.example.befindingjob.dto.admin;

import com.example.befindingjob.entity.User;
import com.example.befindingjob.entity.enumm.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    private String imageUrl;
    private String created_at;
    private String updated_at;

    public UserInfo(User user) {
        this.userId = user.getUserId();
        this.fullName = user.getFullname();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.address = user.getAddress();
        this.dateOfBirth = user.getDateOfBirth().toString();
        this.imageUrl = user.getImageUrl();
    }
}
