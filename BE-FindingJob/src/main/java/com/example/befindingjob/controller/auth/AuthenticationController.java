package com.example.befindingjob.controller.auth;

import com.example.befindingjob.dto.auth.LoginRequest;
import com.example.befindingjob.dto.auth.LoginResponse;
import com.example.befindingjob.dto.auth.RegisterRequest;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ApiResponse<Void> registerUser(@RequestBody RegisterRequest registerRequest) {
        return userService.register(registerRequest);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> registerUser(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }
}
