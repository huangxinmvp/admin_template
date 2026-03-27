package com.hiking.treasure.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {
    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.exp-seconds}")
    private long expSeconds;
    @Value("${app.jwt.refresh-exp-seconds}") // 14d
    private long refreshExpSeconds;

    public String createAccessToken(String uid, String username, String tenantId, String sessionId, List<String> roles, List<String> permissions) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(uid)
                .claim("un", username)
                .claim("tid", tenantId)
                .claim("sid", sessionId)
                .claim("roles", roles)
                .claim("perms", permissions)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String uid, String sessionId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(uid)
                .claim("sid", sessionId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExpSeconds)))
                .claim("rt", true)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token);
    }

    public long getExpSeconds() {
        return expSeconds;
    }

    public long getRefreshExpSeconds() {
        return refreshExpSeconds;
    }
}
