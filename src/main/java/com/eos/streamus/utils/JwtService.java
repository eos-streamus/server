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
  /** Validity of a generated Json Web Token. */
  @Value("${jwt.validityTimeInMinutes}")
  private static final long JWT_VALIDITY_MINUTES = 5L;

  /** Secret key for encoding JWTs. */
  @Value("${jwt.secret}")
  private String key;

  public String createToken(final User user) {
    return Jwts.builder()
               .signWith(Keys.hmacShaKeyFor(key.getBytes()))
               .claim("userId", user.getId())
               .claim("email", user.getEmail())
               .setId(UUID.randomUUID().toString())
               .setIssuedAt(Date.from(Instant.now()))
               .setExpiration(Date.from(Instant.now().plus(JWT_VALIDITY_MINUTES, ChronoUnit.MINUTES)))
               .compact();
  }

  public Jws<Claims> decode(final String jwtToken) {
    return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(key.getBytes()))
        .build()
        .parseClaimsJws(jwtToken);
  }

}
