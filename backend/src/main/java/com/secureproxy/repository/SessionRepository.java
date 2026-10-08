package com.secureproxy.repository;

import com.secureproxy.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository
        extends JpaRepository<Session, UUID> {

    Optional<Session> findByIdAndRevokedFalse(UUID id);

    void deleteByExpiresAtBefore(LocalDateTime time);
}