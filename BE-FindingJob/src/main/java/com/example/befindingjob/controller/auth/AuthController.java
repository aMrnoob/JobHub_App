package com.example.befindingjob.controller.auth;

import com.example.befindingjob.dto.auth.LoginResponse;
import com.example.befindingjob.dto.auth.TokenRequest;
import com.example.befindingjob.entity.User;
import com.example.befindingjob.entity.enumm.Role;
import com.example.befindingjob.model.ApiResponse;
import com.example.befindingjob.repository.UserRepository;
import com.example.befindingjob.service.JwtService;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final String CLIENT_ID = "856354548077-e00ibmh0ojbv416s43qldd8ec0j4o43m.apps.googleusercontent.com"; // Web Client ID

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@RequestBody TokenRequest tokenRequest) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenRequest.getIdToken());
            if (idToken == null) {
                return ResponseEntity.status(401).body(new ApiResponse<>(false, "Invalid Token", null));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            Optional<User> existingUser = userRepository.findByEmail(email);
            User user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                if (user.getFullname() == null && name != null) {
                    user.setFullname(name);
                    userRepository.save(user);
                }
            } else {
                user = createNewUser(email, name);
            }

            String jwtToken = jwtService.generateToken(user.getUserId(), user.getFullname());

            LoginResponse response = new LoginResponse();
            response.setToken(jwtToken);
            response.setUserId(user.getUserId());
            response.setRole(user.getRole());
            response.setFullName(user.getFullname());

            return ResponseEntity.ok(new ApiResponse<>(true, "Login successfully", response));

        } catch (GeneralSecurityException | IOException e) {
            System.err.println("Authentication error Google: " + e.getMessage());
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Authentication failed: " + e.getMessage(), null));
        }
    }

    private User createNewUser(String email, String name) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullname(name);
        newUser.setRole(Role.UNDEFINED);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setPassword("GOOGLE_AUTH");
        return userRepository.save(newUser);
    }
}