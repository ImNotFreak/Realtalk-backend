package real.talk.model.dto.lesson;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {
    private String name;
    private String orderNumber;
    private List<String> youtubeLinks;
    private String email;
    private String telegram;
    private String languageLevel;
    private List<String> grammarTopics;
}
