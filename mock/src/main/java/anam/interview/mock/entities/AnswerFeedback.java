package anam.interview.mock.entities;


import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "answer_feedback")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AnswerFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_question_id", nullable = false, unique = true)
    private SessionQuestion sessionQuestion;

    @Column(name = "has_situation", nullable = false)
    private boolean hasSituation;

    @Column(name = "has_task", nullable = false)
    private boolean hasTask;

    @Column(name = "has_action", nullable = false)
    private boolean hasAction;

    @Column(name = "has_result", nullable = false)
    private boolean hasResult;

    @Column(nullable = false)
    private int score;

    @Column(columnDefinition = "text")
    private String improvement;
}