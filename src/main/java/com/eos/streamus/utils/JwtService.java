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
public final class JwtService {
  /**
   * Expiration time in minutes for token.
   */
  private static final long TOKEN_EXPIRATION = 5L;

  /**
   * JWT secret.
   */
  @Value("${jwt.secret}")
  private String key;

  public String createToken(final User user) {
    return Jwts.builder()
        .signWith(Keys.hmacShaKeyFor(key.getBytes()))
        .claim("userId", user.getId())
        .claim("email", user.getEmail())
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(Date.from(Instant.now()))
        .setExpiration(Date.from(Instant.now().plus(TOKEN_EXPIRATION, ChronoUnit.MINUTES)))
        .compact();
  }

  public Jws<Claims> decode(final String jwtToken) {
    return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(key.getBytes()))
        .build()
        .parseClaimsJws(jwtToken);
  }

}
