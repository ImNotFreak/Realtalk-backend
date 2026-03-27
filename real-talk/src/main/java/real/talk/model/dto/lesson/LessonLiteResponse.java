package real.talk.model.dto.lesson;

import real.talk.model.entity.enums.LessonStatus;

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
        LessonStatus status,
        Tags tags,
        Instant createdAt
) {}
