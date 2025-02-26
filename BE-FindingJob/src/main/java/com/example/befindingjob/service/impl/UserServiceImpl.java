package com.example.befindingjob.service.impl;

import com.example.befindingjob.dto.auth.*;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.repository.UserRepository;
import com.example.befindingjob.service.JwtService;
import com.example.befindingjob.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public ApiResponse<Void> register (RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return new ApiResponse<>(false, "Email đã dùng để đăng ký", null);
        } else if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return new ApiResponse<>(false, "Tên tài khoản đã tồn tại", null);
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setCreatedAt(java.time.LocalDateTime.now());

        userRepository.save(user);

        return new ApiResponse<>(true, "Đăng ký tài khoản thành công");
    }

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest loginRequest) {
        var userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isEmpty()) {
            return new ApiResponse<>(false, "Username không tồn tại");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return new ApiResponse<>(false, "Mật khẩu không chính xác");
        }

        String token = jwtService.generateToken(user.getUserId(), user.getFullname());

        LoginResponse loginResponse = new LoginResponse(token, user.getRole());

        return new ApiResponse<>(true, "Đăng nhập thành công", loginResponse);
    }

    @Override
    public ApiResponse<OtpResponse> forgetPwd(ForgetPwdRequest forgetPwdRequest) {
        Optional<User> userOptional = userRepository.findByEmail(forgetPwdRequest.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(newPassword);
            userRepository.save(user);
            return true;
        }
        return new ApiResponse<>(true, "Đăng nhập thành công", loginResponse);
    }
}
