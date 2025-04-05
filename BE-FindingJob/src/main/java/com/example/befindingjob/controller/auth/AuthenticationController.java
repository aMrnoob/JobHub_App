package com.example.befindingjob.controller.auth;

import com.example.befindingjob.dto.auth.*;
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
    public ApiResponse<Void> registerUser(@RequestBody Register_ResetPwdRequest registerRequest) {
        return userService.register(registerRequest);
    }

    @PostMapping("/otp-register")
    public ApiResponse<Void> otpRegister(@RequestBody OtpRequest otpRequest) {
        return userService.otpRegister(otpRequest);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    @PostMapping("/request")
    public ApiResponse<Void> resetPasswordRequest(@RequestBody OtpRequest otpRequest) {
        return userService.forgetPwdRequest(otpRequest);
    }


    @PostMapping("/verify")
    public ApiResponse<Void> verifyOtp(@RequestBody OtpVerifyRequest otpRequestResponse) {
        return userService.verifyOtpRequest(otpRequestResponse);
    }

    @PostMapping("/reset")
    public ApiResponse<Void> passwordReset(@RequestBody Register_ResetPwdRequest resetPwdRequest) {
        return userService.passwordReset(resetPwdRequest);
    }
}
