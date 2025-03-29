package com.example.befindingjob.service;

import com.example.befindingjob.dto.admin.UserInfo;
import com.example.befindingjob.dto.auth.*;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.model.ApiResponse;

import java.util.Optional;

public interface UserService {
    ApiResponse<Void> register(Register_ResetPwdRequest registerRequest);
    ApiResponse<LoginResponse> login(LoginRequest loginRequest);
    ApiResponse<Void> forgetPwdRequest(ForgetPwdRequest forgetPwdRequest);
    ApiResponse<Void> verifyOtpRequest(OtpVerifyRequest otpVerifyResponse);
    ApiResponse<Void> passwordReset(Register_ResetPwdRequest resetPwdRequest);
    ApiResponse<UserInfo> getUserInfo(String token);
    ApiResponse<Void> updateUser(UserInfo userInfo);

    ApiResponse<Optional<User>> findByEmail(String email);
    ApiResponse<User> createUser(User user);
    ApiResponse<String> generateToken(User user);
    ApiResponse<Boolean> verifyPassword(User user, String password);
}
