package anam.interview.mock.entities;


import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "question")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    @Column(name = "role_tag", length = 100)
    private String roleTag;

    @Column(name = "language_tag", length = 50)
    private String languageTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private QuestionSource source = QuestionSource.SEEDED;

    @Column(name = "occurrence_count", nullable = false)
    @Builder.Default
    private int occurrenceCount = 0;

    public enum QuestionSource { SEEDED, GENERATED }
}