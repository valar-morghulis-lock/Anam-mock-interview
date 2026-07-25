package anam.interview.mock.repositories;


import anam.interview.mock.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
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
}