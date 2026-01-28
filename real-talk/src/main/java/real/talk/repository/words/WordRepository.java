package real.talk.repository.words;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import real.talk.model.entity.words.Word;

import java.util.List;
import java.util.UUID;

public interface WordRepository extends JpaRepository<Word, UUID> {

    List<Word> findByUserUserId(UUID userId);
    List<Word> findByLessonId(UUID lessonId);
    boolean existsByUserUserIdAndLessonId(UUID userId, UUID lessonId);

    Page<Word> findByUserUserId(UUID userId, Pageable pageable);
}
