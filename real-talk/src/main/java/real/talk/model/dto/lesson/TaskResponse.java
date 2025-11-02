package real.talk.model.dto.lesson;

import java.util.List;
import java.util.UUID;

/**
 * Упрощённое представление задания внутри LessonFullResponse.
 */
public record TaskResponse(
        UUID id,
        String type,
        String prompt,
        List<String> items
) {}
