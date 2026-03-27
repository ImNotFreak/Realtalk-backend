package real.talk.model.dto.lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tags {
    private String lesson_theme;
    private String language;
    private String language_level;
    private List<String> lexical_fields;
    private String style;
    private String preset;
    private List<String> grammar_topics;
}
