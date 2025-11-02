package real.talk.model.dto.lesson;

import real.talk.model.dto.lesson.LessonGeneratedByLlm;

import java.time.Instant;
import java.util.UUID;

/**
 * "Лёгкая" мета-информация об уроке для списка (OpenLibrary).
 * Без поля data, но с тегами.
 */
public record LessonLiteResponse(
        UUID id,
        String youtubeUrl,
        String lessonTopic,
        LessonGeneratedByLlm.Tags tags,
        Instant createdAt
) {}
