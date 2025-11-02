package real.talk.model.dto.lesson;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class LessonFilter {
    String language;         // eq
    String languageLevel;    // eq
    String lessonTopic;      // contains (case-insensitive)
    String grammarContains; // contains (case-insensitive) по grammar_topics
    String email; // contains (case-insensitive) по grammar_topics
    Integer page;
    Integer size;
    String sort;
}