package com.example.befindingjob.controller.admin;

import com.example.befindingjob.entity.User;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping("/get-user-info")
    public ApiResponse<User> getUserInfo(@RequestHeader("token") String token) {
        return userService.getUserInfo(token);
    }

    @PostMapping("/update-user")
    public ApiResponse<Void> passwordReset(@RequestBody User user) {
        return userService.updateUser(user);
    }
}
