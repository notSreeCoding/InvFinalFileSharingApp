package com.example.FinalFileSharing.repo;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.FinalFileSharing.model.FileShare;

public interface FileShareRepository extends JpaRepository<FileShare, Long> {

	Optional<FileShare> findByToken(String token);

	Page<FileShare> findByStoredFileOwnerEmailOrderByCreatedAtDesc(String ownerEmail, Pageable pageable);
}
