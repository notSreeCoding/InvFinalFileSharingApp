package com.example.FinalFileSharing.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FinalFileSharing.controller.dto.AuthResponse;
import com.example.FinalFileSharing.controller.dto.LoginRequest;
import com.example.FinalFileSharing.controller.dto.RefreshTokenRequest;
import com.example.FinalFileSharing.controller.dto.RegisterRequest;
import com.example.FinalFileSharing.controller.dto.UserResponse;
import com.example.FinalFileSharing.model.AppUser;
import com.example.FinalFileSharing.model.RefreshToken;
import com.example.FinalFileSharing.repo.RefreshTokenRepository;
import com.example.FinalFileSharing.repo.UserRepository;
import com.example.FinalFileSharing.security.JwtService;
import com.example.FinalFileSharing.support.BadRequestException;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final TokenHashService tokenHashService;
	private final SecureRandom secureRandom = new SecureRandom();
	private final long refreshTokenDays;

	public AuthService(UserRepository userRepository,
			RefreshTokenRepository refreshTokenRepository,
			PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			TokenHashService tokenHashService,
			@Value("${app.jwt.refresh-token-days}") long refreshTokenDays) {
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.tokenHashService = tokenHashService;
		this.refreshTokenDays = refreshTokenDays;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new BadRequestException("Email is already registered.");
		}

		AppUser user = userRepository.save(new AppUser(
				request.name().trim(),
				email,
				passwordEncoder.encode(request.password()),
				"ROLE_USER"));
		return issueTokens(user);
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
		} catch (BadCredentialsException ex) {
			throw new BadRequestException("Invalid email or password.");
		}
		AppUser user = userRepository.findByEmail(email)
				.orElseThrow(() -> new BadRequestException("Invalid email or password."));
		return issueTokens(user);
	}

	@Transactional
	public AuthResponse refresh(RefreshTokenRequest request) {
		String tokenHash = tokenHashService.hash(request.refreshToken());
		RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
				.orElseThrow(() -> new BadRequestException("Refresh token is invalid."));
		if (refreshToken.isRevoked() || refreshToken.isExpired()) {
			throw new BadRequestException("Refresh token is expired or revoked.");
		}
		refreshToken.revoke();
		return issueTokens(refreshToken.getUser());
	}

	@Transactional
	public void logout(RefreshTokenRequest request) {
		refreshTokenRepository.findByTokenHash(tokenHashService.hash(request.refreshToken()))
				.ifPresent(RefreshToken::revoke);
	}

	private AuthResponse issueTokens(AppUser user) {
		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = randomToken();
		refreshTokenRepository.save(new RefreshToken(
				user,
				tokenHashService.hash(refreshToken),
				Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS)));
		return new AuthResponse(accessToken, refreshToken, "Bearer", UserResponse.from(user));
	}

	private String randomToken() {
		byte[] bytes = new byte[64];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
