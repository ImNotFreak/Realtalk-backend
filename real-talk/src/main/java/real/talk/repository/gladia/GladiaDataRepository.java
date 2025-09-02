package real.talk.repository.gladia;

import org.springframework.data.jpa.repository.JpaRepository;
import real.talk.model.entity.GladiaData;

import java.util.UUID;

public interface GladiaDataRepository extends JpaRepository<GladiaData, UUID> {

    GladiaData findGladiaDataByUserUserId(UUID userId);
}
