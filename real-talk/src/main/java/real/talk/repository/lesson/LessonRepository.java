package real.talk.repository.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.enums.LessonStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    Optional<Lesson> findById(UUID lessonId);
    List<Lesson> findByStatus(LessonStatus lessonStatus);
}
