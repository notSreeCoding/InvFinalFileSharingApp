package com.example.FinalFileSharing.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.FinalFileSharing.model.AppUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey signingKey;
	private final long accessTokenMinutes;

	public JwtService(@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.access-token-minutes}") long accessTokenMinutes) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessTokenMinutes = accessTokenMinutes;
	}

	public String generateAccessToken(AppUser user) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(user.getEmail())
				.claim("userId", user.getId())
				.claim("name", user.getName())
				.claim("role", user.getRole())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(accessTokenMinutes * 60)))
				.signWith(signingKey)
				.compact();
	}

	public String usernameFromToken(String token) {
		return claims(token).getSubject();
	}

	public boolean isValid(String token, String username) {
		Claims claims = claims(token);
		return claims.getSubject().equals(username) && claims.getExpiration().after(new Date());
	}

	private Claims claims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
