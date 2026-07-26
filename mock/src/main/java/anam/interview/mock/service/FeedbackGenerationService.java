package anam.interview.mock.service;

import anam.interview.mock.dto.FeedbackReportResponse;
import anam.interview.mock.entities.*;
import anam.interview.mock.entities.TranscriptMessage.Speaker;
import anam.interview.mock.exceptions.ResourceNotFoundException;
import anam.interview.mock.llm.*;
import anam.interview.mock.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackGenerationService {

    private final SessionQuestionRepository sessionQuestionRepository;
    private final TranscriptMessageRepository transcriptMessageRepository;
    private final AnswerFeedbackRepository answerFeedbackRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final InterviewSessionRepository sessionRepository;

    private final LlmClient llmClient;
    private final AnswerFeedbackParser parser;

    @Transactional
    public FeedbackReportResponse generateReport(UUID sessionId) {

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No such session: " + sessionId));

        List<SessionQuestion> answered = sessionQuestionRepository
                .findBySessionIdAndSkippedFalseOrderBySequenceNo(sessionId);

        List<AnswerFeedback> allFeedback = answered.stream()
                .map(this::scoreAnswer)
                .toList();
        answerFeedbackRepository.saveAll(allFeedback);

        String overallStrengths = summarizeStrengths(session, answered, allFeedback);

        FeedbackReport report = FeedbackReport.builder()
                .session(session)
                .overallStrengths(overallStrengths)
                .build();
        report = feedbackReportRepository.save(report);

        List<FeedbackReportResponse.AnswerSummary> answerSummaries = getAnswerSummaries(answered, allFeedback);

        return new FeedbackReportResponse(
                report.getId(), session.getId(), report.getOverallStrengths(),
                report.getCreatedAt(), answerSummaries
        );
    }

    private static List<FeedbackReportResponse.AnswerSummary> getAnswerSummaries(List<SessionQuestion> answered, List<AnswerFeedback> allFeedback) {
        List<FeedbackReportResponse.AnswerSummary> answerSummaries = new ArrayList<>();
        for (int i = 0; i < answered.size(); i++) {
            SessionQuestion sq = answered.get(i);
            AnswerFeedback af = allFeedback.get(i);
            answerSummaries.add(new FeedbackReportResponse.AnswerSummary(
                    sq.getQuestion().getText(),
                    af.isHasSituation(), af.isHasTask(), af.isHasAction(), af.isHasResult(),
                    af.getScore(), af.getImprovement()
            ));
        }
        return answerSummaries;
    }

    private AnswerFeedback scoreAnswer(SessionQuestion sessionQuestion) {
        String candidateAnswer = transcriptMessageRepository
                .findBySessionQuestionIdOrderBySpokenAt(sessionQuestion.getId()).stream()
                .filter(m -> m.getSpeaker() == Speaker.CANDIDATE)
                .map(TranscriptMessage::getContent)
                .collect(Collectors.joining(" "));

        String rawResponse = llmClient.complete(
                FeedbackPromptBuilder.system(),
                FeedbackPromptBuilder.userPrompt(sessionQuestion.getQuestion().getText(), candidateAnswer)
        );

        AnswerAnalysis analysis = parser.parse(rawResponse);

        return AnswerFeedback.builder()
                .sessionQuestion(sessionQuestion)
                .hasSituation(analysis.hasSituation())
                .hasTask(analysis.hasTask())
                .hasAction(analysis.hasAction())
                .hasResult(analysis.hasResult())
                .score(analysis.score())
                .improvement(analysis.improvement())
                .build();
    }

    private String summarizeStrengths(InterviewSession session,
                                      List<SessionQuestion> answered,
                                      List<AnswerFeedback> feedback) {
        String transcriptSummary = answered.stream()
                .map(sq -> "Q: " + sq.getQuestion().getText())
                .collect(Collectors.joining("\n"));

        double avgScore = feedback.stream().mapToInt(AnswerFeedback::getScore).average().orElse(0);

        String prompt = """
                Candidate interviewed for a %s (%s) role. Average STAR score: %.1f/5.
                Questions covered:
                %s

                Write 2-3 sentences summarizing this candidate's overall behavioural interview strengths.
                """.formatted(session.getRole(), session.getSeniority(), avgScore, transcriptSummary);

        return llmClient.complete(
                "You are an expert behavioural interview coach writing a concise summary.",
                prompt
        );
    }
}