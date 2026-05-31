package com.example.FinalFileSharing.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.FinalFileSharing.controller.dto.CreateShareRequest;
import com.example.FinalFileSharing.controller.dto.PublicShareResponse;
import com.example.FinalFileSharing.controller.dto.ShareResponse;
import com.example.FinalFileSharing.model.AppUser;
import com.example.FinalFileSharing.model.FileShare;
import com.example.FinalFileSharing.model.StoredFile;
import com.example.FinalFileSharing.repo.FileShareRepository;
import com.example.FinalFileSharing.repo.StoredFileRepository;
import com.example.FinalFileSharing.support.NotFoundException;

@Service
public class ShareService {

	private final StoredFileRepository fileRepository;
	private final FileShareRepository shareRepository;
	private final ShareEmailSender emailSender;
	private final String publicShareBaseUrl;
	private final String apiBaseUrl;

	public ShareService(StoredFileRepository fileRepository,
			FileShareRepository shareRepository,
			ShareEmailSender emailSender,
			@Value("${app.public-share-base-url}") String publicShareBaseUrl,
			@Value("${app.api-base-url}") String apiBaseUrl) {
		this.fileRepository = fileRepository;
		this.shareRepository = shareRepository;
		this.emailSender = emailSender;
		this.publicShareBaseUrl = publicShareBaseUrl.replaceAll("/+$", "");
		this.apiBaseUrl = apiBaseUrl.replaceAll("/+$", "");
	}

	@Transactional
	public ShareResponse createShare(Long fileId, AppUser owner, CreateShareRequest request) {
		StoredFile file = fileRepository.findByIdAndOwnerEmail(fileId, owner.getEmail())
				.orElseThrow(() -> new NotFoundException("File not found for this user."));

		FileShare share = new FileShare(
				file,
				request.recipientEmail().trim().toLowerCase(),
				request.message().trim(),
				UUID.randomUUID().toString(),
				Instant.now().plus(request.expirationHours(), ChronoUnit.HOURS));

		FileShare saved = shareRepository.save(share);
		String shareUrl = shareUrl(saved.getToken());
		emailSender.sendShareEmail(saved, shareUrl);
		return ShareResponse.from(saved, shareUrl);
	}

	@Transactional(readOnly = true)
	public Page<ShareResponse> listShares(AppUser owner, int page, int size) {
		var pageable = PageRequest.of(page, size);
		return shareRepository
				.findByStoredFileOwnerEmailOrderByCreatedAtDesc(owner.getEmail(), pageable)
				.map(share -> ShareResponse.from(share, shareUrl(share.getToken())));
	}

	@Transactional
	public PublicShareResponse viewShare(String token) {
		FileShare share = validShare(token);
		share.markAccessed();
		return PublicShareResponse.from(share, downloadUrl(token));
	}

	@Transactional
	public FileShare accessForDownload(String token) {
		FileShare share = validShare(token);
		share.markAccessed();
		return share;
	}

	private FileShare validShare(String token) {
		FileShare share = shareRepository.findByToken(token)
				.orElseThrow(() -> new NotFoundException("Share link was not found."));
		if (share.isExpired()) {
			throw new NotFoundException("Share link has expired.");
		}
		return share;
	}

	private String shareUrl(String token) {
		return publicShareBaseUrl + "/public/share/" + token;
	}

	private String downloadUrl(String token) {
		return apiBaseUrl + "/api/public/shares/" + token + "/download";
	}
}