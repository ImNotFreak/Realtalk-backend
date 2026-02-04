package real.talk.model.dto.student;

import java.time.Instant;
import java.util.UUID;

public record StudentResponse(
        UUID id,
        String name,
        String email,
        Instant joinedAt) {
}
