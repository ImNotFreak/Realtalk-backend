package real.talk.model.dto.lesson;

import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonCreateRequest {
    private String name;
    private String orderNumber;
    private List<String> youtubeLinks;
    private String email;
    private String telegram;
    private String language;
    private String languageLevel;
    private List<String> grammarTopics;
}
