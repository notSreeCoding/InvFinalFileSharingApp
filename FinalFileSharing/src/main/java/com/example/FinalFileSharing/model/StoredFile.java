package com.example.FinalFileSharing.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "stored_files")
public class StoredFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(nullable = false)
	private String originalFilename;

	private String contentType;

	@Column(nullable = false)
	private long size;

	@Column(nullable = false, unique = true)
	private String storagePath;

	@Column(nullable = false)
	private Instant uploadedAt;

	protected StoredFile() {
	}

	public StoredFile(AppUser owner, String originalFilename, String contentType, long size, String storagePath) {
		this.owner = owner;
		this.originalFilename = originalFilename;
		this.contentType = contentType;
		this.size = size;
		this.storagePath = storagePath;
		this.uploadedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public AppUser getOwner() {
		return owner;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public String getContentType() {
		return contentType;
	}

	public long getSize() {
		return size;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public Instant getUploadedAt() {
		return uploadedAt;
	}
}
