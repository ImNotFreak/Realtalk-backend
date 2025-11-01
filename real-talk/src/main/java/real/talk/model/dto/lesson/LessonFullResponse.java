package real.talk.model.dto.lesson;

import real.talk.model.dto.lesson.LessonGeneratedByLlm;

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
        LessonGeneratedByLlm.Tags tags,
        List<String> grammarTopics,
        Instant createdAt,
        LessonGeneratedByLlm data
) {}
