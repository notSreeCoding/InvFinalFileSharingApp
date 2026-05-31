package com.example.FinalFileSharing.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.FinalFileSharing.model.AppUser;
import com.example.FinalFileSharing.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	void deleteByUser(AppUser user);
}
