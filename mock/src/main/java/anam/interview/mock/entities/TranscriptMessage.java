package anam.interview.mock.entities;


import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transcript_message")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TranscriptMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_question_id")
    private SessionQuestion sessionQuestion; // nullable — greeting has no question

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Speaker speaker;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "spoken_at", nullable = false)
    @Builder.Default
    private Instant spokenAt = Instant.now();

    public enum Speaker { INTERVIEWER, CANDIDATE }
}