package real.talk.model.dto.lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {
    private Long id;
    private String name;
    private String orderNumber;
    private String youtubeLink;
    private String email;
    private String telegram;
    private String languageLevel;
    private List<String> grammarTopics;
    private OffsetDateTime createdAt;
}