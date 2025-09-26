package real.talk.model.dto.lesson;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LessonFilter {
    String language;         // eq
    String languageLevel;    // eq
    String lessonTopic;      // contains (case-insensitive)
    String grammarContains;  // contains (case-insensitive) по grammar_topics
}