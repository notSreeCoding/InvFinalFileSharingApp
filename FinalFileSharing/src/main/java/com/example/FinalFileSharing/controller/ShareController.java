package com.example.FinalFileSharing.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.FinalFileSharing.controller.dto.CreateShareRequest;
import com.example.FinalFileSharing.controller.dto.ShareResponse;
import com.example.FinalFileSharing.security.AppUserDetails;
import com.example.FinalFileSharing.service.ShareService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Validated
public class ShareController {

	private final ShareService shareService;

	public ShareController(ShareService shareService) {
		this.shareService = shareService;
	}

	@PostMapping("/files/{fileId}/shares")
	@ResponseStatus(HttpStatus.CREATED)
	public ShareResponse shareFile(@PathVariable Long fileId,
			@Valid @RequestBody CreateShareRequest request,
			@AuthenticationPrincipal AppUserDetails userDetails) {
		return shareService.createShare(fileId, userDetails.getUser(), request);
	}

	@GetMapping("/shares")
	public Page<ShareResponse> listShares(@AuthenticationPrincipal AppUserDetails userDetails,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return shareService.listShares(userDetails.getUser(), page, size);
	}
}