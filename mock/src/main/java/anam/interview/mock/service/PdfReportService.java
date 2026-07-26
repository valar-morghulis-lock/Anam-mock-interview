package anam.interview.mock.service;

import anam.interview.mock.entities.*;
import anam.interview.mock.exceptions.ResourceNotFoundException;
import anam.interview.mock.repositories.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final InterviewSessionRepository sessionRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final AnswerFeedbackRepository answerFeedbackRepository;

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD);
    private static final Font HEADING_FONT = new Font(Font.HELVETICA, 13, Font.BOLD);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 11);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.DARK_GRAY);

    @Transactional(readOnly = true)
    public byte[] generatePdf(UUID sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No such session: " + sessionId));

        FeedbackReport report = feedbackReportRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No feedback report yet for session: " + sessionId + " — generate one first via POST /report"));

        List<AnswerFeedback> feedbacks = answerFeedbackRepository.findBySessionIdOrderBySequence(sessionId);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Mock Interview Feedback Report", TITLE_FONT));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph(
                    session.getRole() + " · " + session.getSeniority() + " · "
                            + report.getCreatedAt().atZone(java.time.ZoneOffset.UTC)
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")) + " UTC",
                    BODY_FONT));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Overall Summary", HEADING_FONT));
            document.add(new Paragraph(report.getOverallStrengths(), BODY_FONT));
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Answer Breakdown", HEADING_FONT));
            document.add(Chunk.NEWLINE);

            int i = 1;
            for (AnswerFeedback af : feedbacks) {
                String questionText = af.getSessionQuestion().getQuestion().getText();

                document.add(new Paragraph("Q" + i + ". " + questionText, LABEL_FONT));
                document.add(new Paragraph("Score: " + af.getScore() + " / 5", BODY_FONT));
                document.add(new Paragraph(
                        "STAR — Situation: " + yn(af.isHasSituation())
                                + " · Task: " + yn(af.isHasTask())
                                + " · Action: " + yn(af.isHasAction())
                                + " · Result: " + yn(af.isHasResult()),
                        BODY_FONT));
                document.add(new Paragraph("Improvement: " + af.getImprovement(), BODY_FONT));
                document.add(Chunk.NEWLINE);
                i++;
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate PDF report", e);
        }
    }

    private String yn(boolean present) {
        return present ? "✓" : "✗";
    }
}