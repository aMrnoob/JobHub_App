package com.example.befindingjob.service.impl;

import com.example.befindingjob.dto.admin.UserInfo;
import com.example.befindingjob.dto.auth.*;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.entity.enumm.Role;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.model.OtpEntry;
import com.example.befindingjob.model.OtpStorage;
import com.example.befindingjob.repository.UserRepository;
import com.example.befindingjob.service.EmailService;
import com.example.befindingjob.service.JwtService;
import com.example.befindingjob.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Override
    public ApiResponse<Void> register (Register_ResetPwdRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return new ApiResponse<>(false, "Email exist. Can not register!", null);
        }

        User user = new User();
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(Role.UNDEFINED);
        user.setCreatedAt(java.time.LocalDateTime.now());

        userRepository.save(user);

        return new ApiResponse<>(true, "Register successfully");
    }

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest loginRequest) {
        var userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            return new ApiResponse<>(false, "Email does not exist");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return new ApiResponse<>(false, "Password or Email is incorrect");
        }

        String token = jwtService.generateToken(user.getUserId(), user.getFullname());

        LoginResponse loginResponse = new LoginResponse(token, user.getRole());

        return new ApiResponse<>(true, "Login successfully", loginResponse);
    }

    @Override
    public ApiResponse<Void> forgetPwdRequest(ForgetPwdRequest forgetPwdRequest) {
        var email = forgetPwdRequest.getEmail();
        Optional<User> userOptional = userRepository.findByEmail(forgetPwdRequest.getEmail());
        if (userOptional.isPresent()) {
            String otp = emailService.generateOtp();

            long expiryTime = System.currentTimeMillis() + 60 * 1000;
            OtpStorage.otpStorage.put(email, new OtpEntry(otp, expiryTime));

            boolean isSent = emailService.sendOtp(email, otp);
            if (isSent) {
                return new ApiResponse<>(true, "OTP has sent. Please check your email.");
            } else {
                return new ApiResponse<>(false, "OTP can not send. Please retry.");
            }
        }
        return new ApiResponse<>(false, "Email does not exist");
    }

    @Override
    public ApiResponse<Void> verifyOtpRequest(OtpVerifyRequest otpVerifyResponse) {
        String email = otpVerifyResponse.getEmail();
        String otp = otpVerifyResponse.getOtp();

        OtpEntry otpEntry = OtpStorage.otpStorage.get(email);

        if (otpEntry != null) {
            long currentTime = System.currentTimeMillis();

            if (otpEntry.getOtp().equals(otp) && otpEntry.getExpiryTime() > currentTime) {
                OtpStorage.otpStorage.remove(email);
                return new ApiResponse<>(true, "OTP is valid. You can change your password.");
            } else if (otpEntry.getExpiryTime() <= currentTime) {
                OtpStorage.otpStorage.remove(email);
                return new ApiResponse<>(false, "OTP expired.");
            }
        }

        return new ApiResponse<>(false, "OTP is invalid. Please try again.");
    }

    @Override
    public ApiResponse<Void> passwordReset(Register_ResetPwdRequest resetPwdRequest) {
        String email = resetPwdRequest.getEmail();
        String newPassword = resetPwdRequest.getPassword();

        return userRepository.findByEmail(email).map(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return new ApiResponse<Void>(true, "Reset your password successfully.");
        }).orElseGet(() -> new ApiResponse<>(false, "Reset your password failed. Please try again."));
    }

    @Override
    public ApiResponse<User> getUserInfo(String token) {
        if (!jwtService.isValidToken(token)) {
            return new ApiResponse<>(false, "", null);
        }

        Integer userId = jwtService.extractUserId(token);
        if (userId == null) {
            return new ApiResponse<>(false, "", null);
        }

        return userRepository.findById(userId)
                .map(user -> new ApiResponse<>(true, "", user))
                .orElseGet(() -> new ApiResponse<>(false, "", null));
    }

    @Override
    public ApiResponse<Void> updateUser(UserInfo userInfo) {
        return userRepository.findById(userInfo.getUserId()).map(existingUser -> {
            existingUser.setEmail(userInfo.getEmail());
            if (userInfo.getPassword() != null && !userInfo.getPassword().isBlank()) {
                existingUser.setPassword(passwordEncoder.encode(userInfo.getPassword()));
            }
            existingUser.setRole(userInfo.getRole());
            existingUser.setFullname(userInfo.getFullName());
            existingUser.setAddress(userInfo.getAddress());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate dateOfBirth = LocalDate.parse(userInfo.getDateOfBirth(), formatter);
            existingUser.setDateOfBirth(dateOfBirth.atStartOfDay());
            System.out.println(userInfo.getRole());
            existingUser.setPhone(userInfo.getPhone());

            if (userInfo.getImageUrl() != null && !userInfo.getImageUrl().isBlank()) {
                existingUser.setImageUrl(userInfo.getImageUrl());
            }

            existingUser.setUpdatedAt(LocalDateTime.now());
            userRepository.save(existingUser);
            return new ApiResponse<Void>(true, "User updated successfully", null);
        }).orElseGet(() -> new ApiResponse<>(false, "User not found", null));
    }
    @Override
    public ApiResponse<UserInfo> getUserProfile(String token) {
        if (token == null || !token.startsWith("Bearer "))
            return new ApiResponse<>(false, "Invalid token format");

        token = token.substring(7);
        if (!jwtService.isValidToken(token))
            return new ApiResponse<>(false, "Invalid or expired token");

        int userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfo userInfo = new UserInfo(user);
        return new ApiResponse<>(true, "Success", userInfo);
    }

    @Override
    public ApiResponse<UserInfo> findByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(false, "User not found");
        }
        return new ApiResponse<>(true, "User found");
    }

    @Override
    public ApiResponse<User> createUser(User user) {
        try {
            User savedUser = userRepository.save(user);
            return new ApiResponse<>(true, "User created successfully", savedUser);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Error creating user: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<String> generateToken(User user) {
        try {
            String token = jwtService.generateToken(user.getUserId(), user.getFullname());
            return new ApiResponse<>(true, "Token generated successfully", token);
        } catch (Exception e) {
            return new ApiResponse<>(false, "Error generating token", null);
        }
    }

    @Override
    public ApiResponse<Boolean> verifyPassword(User user, String password) {
        boolean isMatch = passwordEncoder.matches(password, user.getPassword());
        if (isMatch) {
            return new ApiResponse<>(true, "Password verified", true);
        } else {
            return new ApiResponse<>(false, "Invalid password", false);
        }
    }
}
