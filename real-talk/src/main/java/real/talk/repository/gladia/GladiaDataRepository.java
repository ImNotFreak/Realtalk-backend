package real.talk.repository.gladia;

import org.springframework.data.jpa.repository.JpaRepository;
import real.talk.model.entity.GladiaData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GladiaDataRepository extends JpaRepository<GladiaData, UUID> {

    List<GladiaData> findGladiaDataByUserUserId(UUID userId);
    Optional<GladiaData> findGladiaDataByGladiaRequestId(UUID requestId);
}
