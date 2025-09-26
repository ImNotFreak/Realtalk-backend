package real.talk.repository.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.enums.LessonAccess;
import real.talk.model.entity.enums.LessonStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    Optional<Lesson> findById(UUID lessonId);
    List<Lesson> findByStatus(LessonStatus lessonStatus);
    List<Lesson> findByStatusAndAccess(LessonStatus lessonStatus, LessonAccess lessonAccess);
    @Query(value = """
                        SELECT l.* FROM lessons l
                        JOIN gladia_data g ON l.lesson_id = g.lesson_id
                        LEFT JOIN llm_data llm ON l.lesson_id = llm.lesson_id
                        WHERE g.status = 'DONE' AND llm.lesson_id IS NULL""", nativeQuery = true)
    List<Lesson> findProcessingLessonsWithGladiaDone();

    @Query(value = """
                        SELECT l.* FROM lessons l
                        JOIN llm_data llm ON l.lesson_id = llm.lesson_id
                        WHERE l.status != 'READY' AND llm.status = 'DONE'""", nativeQuery = true)
    List<Lesson> findProcessingLessonsWithLlmDone();
@Query(value = """
SELECT l.*
FROM lessons l
WHERE l.status = CAST(:status AS varchar)
AND l.access = CAST(:access AS varchar)
AND (:language IS NULL OR LOWER(TRIM(l.language)) = LOWER(TRIM(:language)))
AND (:level    IS NULL OR LOWER(TRIM(l.language_level)) = LOWER(TRIM(:level)))
AND (:topic    IS NULL OR LOWER(l.lesson_topic) LIKE LOWER(CONCAT('%', :topic, '%')))
AND (:grammar  IS NULL OR LOWER(array_to_string(l.grammar_topics, ', ')) LIKE LOWER(CONCAT('%', :grammar, '%')))
""", nativeQuery = true)
List<Lesson> findPublicReadyFiltered(
        @Param("status") String status,
        @Param("access") String access,
        @Param("language") String language,
        @Param("level") String languageLevel,
        @Param("topic") String lessonTopic,
        @Param("grammar") String grammarContains
    );
}

