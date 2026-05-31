package com.example.FinalFileSharing.controller.dto;

import java.time.Instant;

import com.example.FinalFileSharing.model.StoredFile;

public record FileSummary(
		Long id,
		String fileName,
		String contentType,
		long size,
		Instant uploadedAt) {

	public static FileSummary from(StoredFile file) {
		return new FileSummary(
				file.getId(),
				file.getOriginalFilename(),
				file.getContentType(),
				file.getSize(),
				file.getUploadedAt());
	}
}
