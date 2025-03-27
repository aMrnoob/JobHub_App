package com.example.befindingjob.controller.auth;

import com.example.befindingjob.dto.auth.LoginResponse;
import com.example.befindingjob.dto.auth.TokenRequest;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.entity.enumm.Role;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final String CLIENT_ID = "874208356309-lekou6atac3r23lghn1voe0e7deb5a85.apps.googleusercontent.com"; // Web Client ID

    @Autowired
    private UserService userService;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@RequestBody TokenRequest tokenRequest) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenRequest.getIdToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                User user = userService.findByEmail(email).orElse(null);
                if (user == null) {
                    user = new User();
                    user.setEmail(email);
                    user = userService.createUser(user);
                }

                String token = userService.generateToken(user);
                LoginResponse loginResponse = new LoginResponse(token, user.getRole());
                ApiResponse<LoginResponse> response = new ApiResponse<>(true,  "Login successful", loginResponse);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<LoginResponse> response = new ApiResponse<>(false,  "Invalid token", null);
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            ApiResponse<LoginResponse> response = new ApiResponse<>(false, "Error: " + e.getMessage(), null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
