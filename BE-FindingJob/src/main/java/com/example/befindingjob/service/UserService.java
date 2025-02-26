package com.example.befindingjob.service;

import com.example.befindingjob.dto.auth.*;
import com.example.befindingjob.model.ApiResponse;

public interface UserService {
    ApiResponse<Void> register(RegisterRequest registerRequest);
    ApiResponse<LoginResponse> login(LoginRequest loginRequest);
    ApiResponse<OtpResponse> forgetPwd(ForgetPwdRequest forgetPwdRequest);
}
