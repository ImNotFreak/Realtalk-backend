package real.talk.model.dto.folder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import real.talk.model.dto.lesson.LessonGeneratedByLlm;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderLessonResponse {
    private UUID lessonId;
    private String youtubeUrl;
    private String lessonTopic;
    private LessonGeneratedByLlm.Tags tags;
    private Instant createdAt;
}
