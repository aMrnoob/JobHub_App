package com.example.befindingjob.dto;

import com.example.befindingjob.entity.User;
import com.example.befindingjob.entity.enumm.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
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

    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.fullName = user.getFullname();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.phone = user.getPhone();
        this.address = user.getAddress();
        this.dateOfBirth = (user.getDateOfBirth() != null) ? user.getDateOfBirth().toString() : null;
        this.imageUrl = user.getImageUrl();
    }
}
