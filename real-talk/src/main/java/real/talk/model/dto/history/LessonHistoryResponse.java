package real.talk.model.dto.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import real.talk.model.dto.lesson.LessonGeneratedByLlm;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonHistoryResponse {
    private UUID lessonId;
    private String youtubeUrl;
    private String lessonTopic;
    private LessonGeneratedByLlm.Tags tags;
    private Instant createdAt;
    private Instant lastOpenedAt;

}
