package real.talk.repository.words;

import org.springframework.data.jpa.repository.JpaRepository;
import real.talk.model.entity.User;
import real.talk.model.entity.words.WordSet;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WordSetRepository extends JpaRepository<WordSet, UUID> {
    List<WordSet> findByUserUserId(UUID userId);
    Optional<WordSet> findByIdAndUserUserId(UUID id, UUID userId);
    long countByUser(User user);
}
