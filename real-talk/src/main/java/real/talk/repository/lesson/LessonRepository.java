package real.talk.repository.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
                        WHERE llm.status = 'DONE'""", nativeQuery = true)
    List<Lesson> findProcessingLessonsWithLlmDone();


}
