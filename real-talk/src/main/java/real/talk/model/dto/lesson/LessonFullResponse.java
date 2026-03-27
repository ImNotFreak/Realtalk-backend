package real.talk.model.dto.lesson;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO для полного урока (используется при запросе по ID).
 * Возвращает все данные урока, включая JSON data (LessonGeneratedByLlm).
 */
public record LessonFullResponse(
        UUID id,
        String youtubeUrl,
        String lessonTopic,
        Tags tags,
        List<String> grammarTopics,
        Instant createdAt,
        GeneratedPreset data
) {}
