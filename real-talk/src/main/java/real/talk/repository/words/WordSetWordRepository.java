package real.talk.repository.words;

import org.springframework.data.jpa.repository.JpaRepository;
import real.talk.model.entity.words.WordSetWord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WordSetWordRepository extends JpaRepository<WordSetWord, UUID> {

    List<WordSetWord> findByWordSetIdOrderByPositionAsc(UUID wordSetId);
    Optional<WordSetWord> findByWordSetIdAndWordId(UUID wordSetId, UUID wordId);
    void deleteByWordSetIdAndWordId(UUID wordSetId, UUID wordId);
    void deleteByWordSetIdAndWordIdIn(UUID wordSetId, List<UUID> wordIds);
}
