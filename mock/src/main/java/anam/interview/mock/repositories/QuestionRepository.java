package anam.interview.mock.repositories;


import anam.interview.mock.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByCompetencyIdIn(List<UUID> competencyIds);

    /*
     - Performance Enhancement - Memory Wise
     Random selection at the DB level, not by pulling all rows into the app*/
    @Query(value = """
        SELECT * FROM question
        WHERE competency_id = :competencyId
        ORDER BY random()
        LIMIT :limit
        """, nativeQuery = true)
    List<Question> findRandomByCompetency(@Param("competencyId") UUID competencyId,
                                          @Param("limit") int limit);


    @Query("""
    SELECT COUNT(q) FROM Question q
    WHERE q.competency.id = :competencyId
    AND q.roleTag = :roleTag
    AND (:languageTag IS NULL OR q.languageTag = :languageTag)
    """)
    long countByCompetencyAndRoleAndLanguage(@Param("competencyId") UUID competencyId,
                                             @Param("roleTag") String roleTag,
                                             @Param("languageTag") String languageTag);

    @Query(value = """
    SELECT * FROM question
    WHERE competency_id = :competencyId
    AND similarity(text, :candidateText) > 0.8
    LIMIT 1
    """, nativeQuery = true)
    Optional<Question> findSimilar(@Param("competencyId") UUID competencyId,
                                   @Param("candidateText") String candidateText);

    @Query(value = """
    SELECT * FROM question
    WHERE competency_id = :competencyId
    AND role_tag = :roleTag
    AND (:languageTag IS NULL AND language_tag IS NULL OR language_tag = :languageTag)
    ORDER BY random()
    LIMIT :limit
    """, nativeQuery = true)
    List<Question> findRandomByCompetencyAndTags(@Param("competencyId") UUID competencyId,
                                                 @Param("roleTag") String roleTag,
                                                 @Param("languageTag") String languageTag,
                                                 @Param("limit") int limit);
}