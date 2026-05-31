package com.example.FinalFileSharing.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.FinalFileSharing.controller.dto.FileSummary;
import com.example.FinalFileSharing.security.AppUserDetails;
import com.example.FinalFileSharing.service.FileStorageService;

@RestController
@RequestMapping("/api/files")
public class FileController {

	private final FileStorageService fileStorageService;

	public FileController(FileStorageService fileStorageService) {
		this.fileStorageService = fileStorageService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FileSummary upload(@RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal AppUserDetails userDetails) {
		return FileSummary.from(fileStorageService.store(file, userDetails.getUser()));
	}

	@GetMapping
	public Page<FileSummary> list(@AuthenticationPrincipal AppUserDetails userDetails,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return fileStorageService.listOwnedFiles(userDetails.getUser(), page, size);
	}
}