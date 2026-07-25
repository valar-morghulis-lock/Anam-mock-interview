package anam.interview.mock.service;

import anam.interview.mock.entities.InterviewSession;
import anam.interview.mock.entities.InterviewSession.SessionStatus;
import anam.interview.mock.exceptions.InvalidSessionStateException;
import anam.interview.mock.exceptions.ResourceNotFoundException;
import anam.interview.mock.repositories.InterviewSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionLifecycleService {

    private final InterviewSessionRepository sessionRepository;

    @Transactional
    public void markStarted(UUID sessionId) {
        InterviewSession session = getSession(sessionId);
        if (session.getStatus() != SessionStatus.CREATED) {
            throw new InvalidSessionStateException("Session already started or finished: " + sessionId);
        }
        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setStartedAt(Instant.now());
    }

    @Transactional
    public void markEnded(UUID sessionId, boolean completedNaturally) {
        InterviewSession session = getSession(sessionId);
        session.setStatus(completedNaturally ? SessionStatus.COMPLETED : SessionStatus.ABANDONED);
        session.setEndedAt(Instant.now());
    }

    private InterviewSession getSession(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No such session: " + sessionId));
    }
}