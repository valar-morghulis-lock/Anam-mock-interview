package anam.interview.mock.service;

import anam.interview.mock.entities.*;
import anam.interview.mock.exceptions.ResourceNotFoundException;
import anam.interview.mock.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TranscriptService {

    private final InterviewSessionRepository sessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;
    private final TranscriptMessageRepository transcriptMessageRepository;

    @Transactional
    public void appendMessage(UUID sessionId, UUID sessionQuestionId,
                              TranscriptMessage.Speaker speaker, String content) {

        InterviewSession session = sessionRepository.getReferenceById(sessionId);
        SessionQuestion sessionQuestion = sessionQuestionId != null
                ? sessionQuestionRepository.getReferenceById(sessionQuestionId)
                : null;

        transcriptMessageRepository.save(TranscriptMessage.builder()
                .session(session)
                .sessionQuestion(sessionQuestion)
                .speaker(speaker)
                .content(content)
                .build());
    }

    @Transactional
    public void markSkipped(UUID sessionQuestionId) {
        SessionQuestion sq = sessionQuestionRepository.findById(sessionQuestionId)
                .orElseThrow(() -> new ResourceNotFoundException("No such session question: " + sessionQuestionId));
        sq.setSkipped(true);
    }

    public List<TranscriptMessage> getTranscript(UUID sessionId) {
        return transcriptMessageRepository.findBySessionIdOrderBySpokenAt(sessionId);
    }
}