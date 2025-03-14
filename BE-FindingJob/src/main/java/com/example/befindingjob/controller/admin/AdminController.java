package com.example.befindingjob.controller.admin;

import com.example.befindingjob.dto.auth.OtpVerifyRequest;
import com.example.befindingjob.dto.auth.Register_ResetPwdRequest;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/get-user-info")
    public ApiResponse<User> getUserInfo(@RequestBody String token) {
        return userService.getUserInfo(token);
    }

    @PostMapping("/update-user")
    public ApiResponse<Void> passwordReset(@RequestBody User user) {
        return userService.updateUser(user);
    }
}
