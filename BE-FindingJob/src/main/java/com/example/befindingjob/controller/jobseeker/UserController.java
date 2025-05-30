package com.example.befindingjob.controller.jobseeker;

import com.example.befindingjob.dto.UserDTO;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.entity.enumm.Role;
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
    public ApiResponse<UserDTO> getUser(@RequestHeader("token") String token) {
        return userService.getUser(token);
    }

    @GetMapping("/find-by-email")
    public ApiResponse<UserDTO> findByEmail(@RequestHeader("email") String email) {
        return userService.findByEmail(email);
    }

    @DeleteMapping("/delete-account")
    public ApiResponse<UserDTO> deleteAccount(@RequestHeader("token") String token) {
        return userService.deleteAccountUser(token);
    }

}
