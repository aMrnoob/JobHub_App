package com.example.befindingjob.controller.jobseeker;

import com.example.befindingjob.dto.admin.UserInfo;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.JwtService;
import com.example.befindingjob.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/update-user")
    public ResponseEntity<ApiResponse<Void>> updateUser(@RequestBody UserInfo userInfo) {
        ApiResponse<Void> response = userService.updateUser(userInfo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUserProfile(@RequestHeader("token") String token) {
        try {
            UserInfo userInfo = userService.getUserProfile(token).getData();
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy thông tin thành công", userInfo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, "Token không hợp lệ", null));
        }
    }

}
