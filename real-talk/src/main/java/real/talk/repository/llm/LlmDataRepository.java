package real.talk.repository.llm;

import org.springframework.data.jpa.repository.JpaRepository;
import real.talk.model.entity.LlmData;

import java.util.Optional;
import java.util.UUID;

public interface LlmDataRepository extends JpaRepository<LlmData, UUID> {

    Optional<LlmData> findById(UUID uuid);
}
