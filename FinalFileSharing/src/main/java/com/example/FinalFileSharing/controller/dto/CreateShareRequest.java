package com.example.FinalFileSharing.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateShareRequest(
		@NotBlank @Email String recipientEmail,
		@Min(1) @Max(8760) int expirationHours,
		@NotBlank String message) {
}
