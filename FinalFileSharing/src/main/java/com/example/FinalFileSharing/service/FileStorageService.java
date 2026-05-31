package com.example.FinalFileSharing.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.FinalFileSharing.controller.dto.FileSummary;
import com.example.FinalFileSharing.model.AppUser;
import com.example.FinalFileSharing.model.StoredFile;
import com.example.FinalFileSharing.repo.StoredFileRepository;
import com.example.FinalFileSharing.support.BadRequestException;
import com.example.FinalFileSharing.support.NotFoundException;

@Service
public class FileStorageService {

	private final Path storageRoot;
	private final StoredFileRepository fileRepository;

	public FileStorageService(@Value("${app.files.storage-dir}") String storageDir,
			StoredFileRepository fileRepository) {
		this.storageRoot = Path.of(storageDir).toAbsolutePath().normalize();
		this.fileRepository = fileRepository;
	}

	public StoredFile store(MultipartFile upload, AppUser owner) {
		if (upload.isEmpty()) {
			throw new BadRequestException("Please choose a non-empty file.");
		}

		try {
			Files.createDirectories(storageRoot);

			String originalName = StringUtils.cleanPath(
					upload.getOriginalFilename() == null ? "file" : upload.getOriginalFilename());

			String extension = "";
			int dotIndex = originalName.lastIndexOf('.');
			if (dotIndex >= 0) {
				extension = originalName.substring(dotIndex);
			}

			String storedName = UUID.randomUUID() + extension;
			Path destination = storageRoot.resolve(storedName).normalize();

			try (InputStream inputStream = upload.getInputStream()) {
				Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
			}

			return fileRepository.save(
					new StoredFile(owner, originalName, upload.getContentType(), upload.getSize(), storedName));
		} catch (IOException ex) {
			throw new BadRequestException("Could not store the uploaded file.");
		}
	}

	public Page<FileSummary> listOwnedFiles(AppUser owner, int page, int size) {
		var pageable = PageRequest.of(page, size);
		return fileRepository
				.findByOwnerEmailOrderByUploadedAtDesc(owner.getEmail(), pageable)
				.map(FileSummary::from);
	}

	public Resource loadAsResource(StoredFile file) {
		try {
			Path filePath = storageRoot.resolve(file.getStoragePath()).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists() && resource.isReadable()) {
				return resource;
			}
			throw new NotFoundException("Stored file could not be read.");
		} catch (IOException ex) {
			throw new NotFoundException("Stored file could not be read.");
		}
	}
}