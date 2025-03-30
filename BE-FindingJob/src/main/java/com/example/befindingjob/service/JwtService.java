package com.example.befindingjob.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private Key getSigningKey() {
        String SECRET_KEY = "G6H1D9K8B5F4M0L3N2P7A8B5F4M0L1C7Q9E8B5F4M0L2R6X3Y4VZT";
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(Integer userId, String fullname) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("fullname", fullname);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractFullname(String token) {
        token = token.substring(7);
        return extractClaim(token, claims -> claims.get("fullname", String.class));
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Integer extractUserId(String token) {
        token = token.substring(7);
        return extractClaim(token, claims -> claims.get("userId", Integer.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    public boolean isValidToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return false;
        }

        String pureToken = token.substring(7);

        try {
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(pureToken)
                    .getBody();
            return !isTokenExpired(pureToken);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
