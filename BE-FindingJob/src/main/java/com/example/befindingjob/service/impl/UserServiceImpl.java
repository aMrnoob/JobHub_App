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
    public ApiResponse<UserInfo> getUserInfo(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return new ApiResponse<>(false, "Invalid token format. Token must start with 'Bearer '.");
        }

        token = token.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return new ApiResponse<>(false, "Invalid or expired token.");
        }

        Integer userId = jwtService.extractUserId(token);
        if (userId == null) {
            return new ApiResponse<>(false, "Invalid token: Missing user information.");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return new ApiResponse<>(false, "User not found.");
        }

        User user = userOptional.get();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setFullName(user.getFullname());
        userInfo.setEmail(user.getEmail());
        userInfo.setRole(user.getRole());
        userInfo.setAddress(user.getAddress());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (user.getDateOfBirth() != null) {
            userInfo.setDateOfBirth(user.getDateOfBirth().format(formatter));
        } else {
            userInfo.setDateOfBirth(null);
        }

        if (user.getCreatedAt() != null) {
            userInfo.setCreated_at(user.getCreatedAt().format(formatter));
        } else {
            userInfo.setCreated_at(null);
        }

        if (user.getUpdatedAt() != null) {
            userInfo.setUpdated_at(user.getUpdatedAt().format(formatter));
        } else {
            userInfo.setUpdated_at(null);
        }

        userInfo.setPhone(user.getPhone());


        return new ApiResponse<>(true, "", userInfo);
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
            existingUser.setUpdatedAt(java.time.LocalDateTime.now());

            userRepository.save(existingUser);
            return new ApiResponse<Void>(true, "User updated successfully", null);
        }).orElseGet(() -> new ApiResponse<>(false, "User not found", null));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(User user) {
        user.setRole(Role.UNDEFINED);
        return userRepository.save(user);
    }

    @Override
    public String generateToken(User user) {
        return "sample-jwt-token-" + user.getEmail();
    }

    @Override
    public boolean verifyPassword(User user, String password) {
        return user.getPassword() != null && user.getPassword().equals(password);
    }

}
