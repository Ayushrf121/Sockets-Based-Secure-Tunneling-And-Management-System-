package com.secureproxy.service;

import com.secureproxy.model.Session;
import com.secureproxy.model.User;
import com.secureproxy.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionService {

    private static final long SESSION_HOURS = 1;

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Session createSession(
            User user,
            String deviceId,
            String ipAddress) {

        Session session = new Session();

        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setDeviceId(deviceId);
        session.setIpAddress(ipAddress);

        LocalDateTime now = LocalDateTime.now();

        session.setCreatedAt(now);
        session.setExpiresAt(
                now.plusHours(SESSION_HOURS)
        );

        session.setRevoked(false);

        return sessionRepository.save(session);
    }

    public Session getValidSession(UUID sessionId) {

        Session session = sessionRepository
                .findByIdAndRevokedFalse(sessionId)
                .orElse(null);

        if (session == null) {
            return null;
        }

        if (session.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            session.setRevoked(true);
            sessionRepository.save(session);

            return null;
        }

        return session;
    }

    public void revokeSession(UUID sessionId) {

        Session session = sessionRepository
                .findById(sessionId)
                .orElse(null);

        if (session != null) {
            session.setRevoked(true);
            sessionRepository.save(session);
        }
    }
}