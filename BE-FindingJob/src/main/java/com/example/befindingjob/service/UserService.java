package com.example.befindingjob.service;

import com.example.befindingjob.dto.UserDTO;
import com.example.befindingjob.dto.auth.*;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.entity.enumm.Role;
import com.example.befindingjob.model.ApiResponse;

public interface UserService {
    ApiResponse<Void> register(Register_ResetPwdRequest registerRequest);
    ApiResponse<Void> otpRegister(OtpRequest otpRequest);
    ApiResponse<LoginResponse> login(LoginRequest loginRequest);
    ApiResponse<Void> forgetPwdRequest(OtpRequest otpRequest);
    ApiResponse<Void> verifyOtpRequest(OtpVerifyRequest otpVerifyResponse);
    ApiResponse<Void> passwordReset(Register_ResetPwdRequest resetPwdRequest);
    ApiResponse<Void> updateUser(UserDTO userDTO);

    ApiResponse<UserDTO> getUser(String token);
    ApiResponse<UserDTO> deleteAccountUser(String token);
    ApiResponse<UserDTO> findByEmail(String email);
}
