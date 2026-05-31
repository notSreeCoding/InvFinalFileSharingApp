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
@Table(name = "file_shares")
public class FileShare {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stored_file_id", nullable = false)
	private StoredFile storedFile;

	@Column(nullable = false)
	private String recipientEmail;

	@Column(length = 2000)
	private String message;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean accessed;

	private Instant accessedAt;

	protected FileShare() {
	}

	public FileShare(StoredFile storedFile, String recipientEmail, String message, String token, Instant expiresAt) {
		this.storedFile = storedFile;
		this.recipientEmail = recipientEmail;
		this.message = message;
		this.token = token;
		this.createdAt = Instant.now();
		this.expiresAt = expiresAt;
	}

	public Long getId() {
		return id;
	}

	public StoredFile getStoredFile() {
		return storedFile;
	}

	public String getRecipientEmail() {
		return recipientEmail;
	}

	public String getMessage() {
		return message;
	}

	public String getToken() {
		return token;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public boolean isAccessed() {
		return accessed;
	}

	public Instant getAccessedAt() {
		return accessedAt;
	}

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}

	public void markAccessed() {
		if (!accessed) {
			accessed = true;
			accessedAt = Instant.now();
		}
	}
}
