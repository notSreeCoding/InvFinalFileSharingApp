package com.example.FinalFileSharing.repo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.FinalFileSharing.model.StoredFile;

public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

	Page<StoredFile> findByOwnerEmailOrderByUploadedAtDesc(String email, Pageable pageable);

	Optional<StoredFile> findByIdAndOwnerEmail(Long id, String ownerEmail);
}
