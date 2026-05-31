package com.example.FinalFileSharing.controller.dto;

import com.example.FinalFileSharing.model.AppUser;

public record UserResponse(
		Long id,
		String name,
		String email,
		String role) {

	public static UserResponse from(AppUser user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
	}
}
