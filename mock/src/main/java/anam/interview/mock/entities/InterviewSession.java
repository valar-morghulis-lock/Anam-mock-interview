package anam.interview.mock.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "interview_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Seniority seniority;

    @Enumerated(EnumType.STRING)
    @Column(name = "persona_style", nullable = false, length = 30)
    private PersonaStyle personaStyle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.CREATED;

    @Column(name = "time_limit_sec", nullable = false)
    private int timeLimitSec;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "session_competency", joinColumns = @JoinColumn(name = "session_id"), inverseJoinColumns = @JoinColumn(name = "competency_id"))
    @Builder.Default
    private Set<Competency> competencies = new HashSet<>();

    public enum Seniority {JUNIOR, MID, SENIOR, STAFF}

    public enum PersonaStyle {SUPPORTIVE, BAR_RAISER}

    public enum SessionStatus {CREATED, IN_PROGRESS, COMPLETED, ABANDONED}
}