package anam.interview.mock.entities;


import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_question",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "sequence_no"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Column(nullable = false)
    @Builder.Default
    private boolean skipped = false;

    @Column(name = "asked_at")
    private Instant askedAt;
}