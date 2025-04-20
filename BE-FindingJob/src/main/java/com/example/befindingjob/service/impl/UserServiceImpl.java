package com.example.befindingjob.service.impl;

import com.example.befindingjob.dto.UserDTO;
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
            return new ApiResponse<>(false, "", null);
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
    public ApiResponse<Void> otpRegister(OtpRequest otpRequest) {
        var email = otpRequest.getEmail();
        Optional<User> userOptional = userRepository.findByEmail(otpRequest.getEmail());

        if (userOptional.isEmpty()) {
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
        return new ApiResponse<>(false, "Email has been registered. Please select another email");
    }

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest loginRequest) {
        var userOptional = userRepository.findByEmail(loginRequest.getEmail());
        if (userOptional.isEmpty()) {
            return new ApiResponse<>(false, "Email does not exist");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return new ApiResponse<>(false, "Email or Password is incorrect");
        }

        String token = jwtService.generateToken(user.getUserId(), user.getFullname());

        LoginResponse loginResponse = new LoginResponse(token, user.getUserId(),user.getRole(), user.getFullname());

        return new ApiResponse<>(true, "Login successfully", loginResponse);
    }

    @Override
    public ApiResponse<Void> forgetPwdRequest(OtpRequest otpRequest) {
        var email = otpRequest.getEmail();
        Optional<User> userOptional = userRepository.findByEmail(otpRequest.getEmail());
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
                return new ApiResponse<>(true, "");
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
    public ApiResponse<Void> updateUser(UserDTO userDTO) {
        return userRepository.findById(userDTO.getUserId()).map(existingUser -> {
            existingUser.setEmail(userDTO.getEmail());
            if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
                existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }
            existingUser.setRole(userDTO.getRole());
            existingUser.setFullname(userDTO.getFullName());
            existingUser.setAddress(userDTO.getAddress());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
            LocalDate dateOfBirth = LocalDate.parse(userDTO.getDateOfBirth(), formatter);
            existingUser.setDateOfBirth(dateOfBirth.atStartOfDay());
            existingUser.setPhone(userDTO.getPhone());

            if (userDTO.getImageUrl() != null && !userDTO.getImageUrl().isBlank()) {
                existingUser.setImageUrl(userDTO.getImageUrl());
            }

            existingUser.setUpdatedAt(LocalDateTime.now());

            userRepository.save(existingUser);
            return new ApiResponse<Void>(true, "User updated successfully", null);
        }).orElseGet(() -> new ApiResponse<>(false, "User updated failed", null));
    }

    @Override
    public ApiResponse<UserDTO> getUser(String token) {
        if (!jwtService.isValidToken(token))
            return new ApiResponse<>(false, "Invalid or expired token");

        int userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDTO userDTO = new UserDTO(user);

        return new ApiResponse<>(true, "", userDTO);
    }

    @Override
    public ApiResponse<UserDTO> deleteAccountUser(String token) {
        if (!jwtService.isValidToken(token)) {
            return new ApiResponse<>(false, "Delete account failed");
        }

        int userId = jwtService.extractUserId(token);

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>(false, "Delete account failed");
        }

        userRepository.deleteById(userId);
        return new ApiResponse<>(true, "Delete account successfully");
    }

    @Override
    public ApiResponse<UserDTO> findByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(false, "");
        }
        return new ApiResponse<>(true, "");
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
    public ApiResponse<Boolean> verifyPassword(User user, String password) {
        boolean isMatch = passwordEncoder.matches(password, user.getPassword());
        if (isMatch) {
            return new ApiResponse<>(true, "Password verified", true);
        } else {
            return new ApiResponse<>(false, "Invalid password", false);
        }
    }
}
