package com.example.FinalFileSharing.controller.dto;

public record AuthResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		UserResponse user) {
}
