package anam.interview.mock.service;

import anam.interview.mock.entities.*;
import anam.interview.mock.dto.InterviewSetupRequest;
import anam.interview.mock.dto.InterviewSessionResponse;
import anam.interview.mock.repositories.*;
import anam.interview.mock.util.TagNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewSetupService {

    private final CompetencyRepository competencyRepository;
    private final QuestionRepository questionRepository;
    private final InterviewSessionRepository sessionRepository;
    private final SessionQuestionRepository sessionQuestionRepository;
    private final QuestionGenerationService questionGenerationService;

    @Transactional
    public InterviewSessionResponse createSession(InterviewSetupRequest request) {

        List<Competency> competencies = request.competencyNames().stream().map(name -> competencyRepository.findByName(name).orElseThrow(() -> new IllegalArgumentException("Unknown competency: " + name))).toList();

        InterviewSession session = InterviewSession.builder().role(request.role()).seniority(request.seniority()).personaStyle(request.personaStyle()).timeLimitSec(request.timeLimitSec()).competencies(new HashSet<>(competencies)).build();
        session = sessionRepository.save(session);

        List<SessionQuestion> sessionQuestions = new ArrayList<>();
        int sequenceNo = 1;

        for (Competency competency : competencies) {
            List<Question> picked = isTechnical(competency) ? pickTechnicalQuestions(competency, request) : questionRepository.findRandomByCompetency(competency.getId(), request.questionsPerCompetency());

            for (Question question : picked) {
                sessionQuestions.add(SessionQuestion.builder().session(session).question(question).sequenceNo(sequenceNo++).build());
            }
        }

        sessionQuestions = sessionQuestionRepository.saveAll(sessionQuestions);

        var summaries = sessionQuestions.stream().map(sq -> new InterviewSessionResponse.QuestionSummary(sq.getId(), sq.getQuestion().getText(), sq.getSequenceNo())).toList();

        return new InterviewSessionResponse(session.getId(), session.getRole(), session.getSeniority().name(), session.getPersonaStyle().name(), summaries);
    }

    private boolean isTechnical(Competency competency) {
        return "technical".equals(competency.getName());
    }

    private List<Question> pickTechnicalQuestions(Competency competency, InterviewSetupRequest request) {
        String roleTag = TagNormalizer.normalize(request.role());
        String languageTag = TagNormalizer.normalize(request.language());

        questionGenerationService.ensurePoolIsStocked("technical", roleTag, languageTag);
        return questionRepository.findRandomByCompetencyAndTags(
                competency.getId(), roleTag, languageTag, request.questionsPerCompetency());
    }
}