package com.example.befindingjob.controller.jobseeker;

import com.example.befindingjob.dto.UserDTO;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.JwtService;
import com.example.befindingjob.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/update-user")
    public ResponseEntity<ApiResponse<Void>> updateUser(@RequestBody UserDTO userDTO) {
        ApiResponse<Void> response = userService.updateUser(userDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUser(@RequestHeader("token") String token) {
        try {
            UserDTO userDTO = userService.getUser(token).getData();
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy thông tin thành công", userDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, "Token không hợp lệ", null));
        }
    }
}
