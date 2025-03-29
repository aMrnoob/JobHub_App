package com.example.befindingjob.controller.jobseeker;

import com.example.befindingjob.dto.admin.UserInfo;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/update-user")
    public ResponseEntity<ApiResponse<Void>> updateUser(@RequestBody UserInfo userInfo) {
        ApiResponse<Void> response = userService.updateUser(userInfo);
        return ResponseEntity.ok(response);
    }
}
