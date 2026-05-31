package com.example.FinalFileSharing.controller.dto;

import java.time.Instant;

import com.example.FinalFileSharing.model.FileShare;

public record ShareResponse(
		Long id,
		Long fileId,
		String fileName,
		String recipientEmail,
		Instant shareDate,
		Instant expiresAt,
		boolean accessed,
		Instant accessedAt,
		String shareUrl) {

	public static ShareResponse from(FileShare share, String shareUrl) {
		return new ShareResponse(
				share.getId(),
				share.getStoredFile().getId(),
				share.getStoredFile().getOriginalFilename(),
				share.getRecipientEmail(),
				share.getCreatedAt(),
				share.getExpiresAt(),
				share.isAccessed(),
				share.getAccessedAt(),
				shareUrl);
	}
}
