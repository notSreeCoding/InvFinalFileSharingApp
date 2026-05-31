package com.example.FinalFileSharing.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FinalFileSharing.controller.dto.PublicShareResponse;
import com.example.FinalFileSharing.model.FileShare;
import com.example.FinalFileSharing.service.FileStorageService;
import com.example.FinalFileSharing.service.ShareService;

@RestController
@RequestMapping("/api/public/shares")
public class PublicShareController {

	private final ShareService shareService;
	private final FileStorageService fileStorageService;

	public PublicShareController(ShareService shareService, FileStorageService fileStorageService) {
		this.shareService = shareService;
		this.fileStorageService = fileStorageService;
	}

	@GetMapping("/{token}")
	public PublicShareResponse view(@PathVariable String token) {
		return shareService.viewShare(token);
	}

	@GetMapping("/{token}/download")
	public ResponseEntity<Resource> download(@PathVariable String token) {
		FileShare share = shareService.accessForDownload(token);
		Resource resource = fileStorageService.loadAsResource(share.getStoredFile());
		String contentType = share.getStoredFile().getContentType() == null
				? MediaType.APPLICATION_OCTET_STREAM_VALUE
				: share.getStoredFile().getContentType();

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(share.getStoredFile().getOriginalFilename(), StandardCharsets.UTF_8)
						.build()
						.toString())
				.body(resource);
	}
}
