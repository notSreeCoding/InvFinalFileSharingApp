package com.example.FinalFileSharing.controller.dto;

import java.time.Instant;

import com.example.FinalFileSharing.model.FileShare;

public record PublicShareResponse(
		String fileName,
		String contentType,
		long size,
		String message,
		String recipientEmail,
		Instant expiresAt,
		String downloadUrl) {

	public static PublicShareResponse from(FileShare share, String downloadUrl) {
		return new PublicShareResponse(
				share.getStoredFile().getOriginalFilename(),
				share.getStoredFile().getContentType(),
				share.getStoredFile().getSize(),
				share.getMessage(),
				share.getRecipientEmail(),
				share.getExpiresAt(),
				downloadUrl);
	}
}
