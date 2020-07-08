package com.eos.streamus.utils;

import com.eos.streamus.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class JwtService {
  @Value("${jwt.secret}")
  private String key;

  public String createToken(User user) {
    return Jwts.builder()
               .signWith(Keys.hmacShaKeyFor(key.getBytes()))
               .claim("userId", user.getId())
               .claim("email", user.getEmail())
               .setId(UUID.randomUUID().toString())
               .setIssuedAt(Date.from(Instant.now()))
               .setExpiration(Date.from(Instant.now().plus(5l, ChronoUnit.MINUTES)))
               .compact();
  }

  public Jws<Claims> decode(String jwtToken) {
    return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(key.getBytes()))
        .build()
        .parseClaimsJws(jwtToken);
  }

}
