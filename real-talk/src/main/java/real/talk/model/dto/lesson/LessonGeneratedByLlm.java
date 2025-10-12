package real.talk.model.dto.lesson;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonGeneratedByLlm {
    private String language;
    private String language_level;
    private String transcript_text;
    private LanguageFilter language_filter;
    private Tags tags;
    private List<String> grammar_topics;
    private List<GlossaryItem> glossary;
    private Exercises exercises;
    private List<String> quizlet;
    private String you_tube_url;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageFilter {
        private String target_language;
        private boolean ignore_other_languages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tags {
        private String lesson_theme;
        private String language;
        private String level;
        private List<String> lexical_fields;
        private String style;
        private List<String> grammar;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlossaryItem {
        private String term;
        private String quote;
        private String translation_ru;
        private String explanation_ru;
        private String another_example;
        private Double timeCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Exercises {
        private Map<String, LexicalExercise> lexical;
        private Map<String, GrammarExercise> grammar;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LexicalExercise {
        private String phrases_pool;
        private String note;
        private List<Item> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrammarExercise {
        private String instructions;
        private List<Item> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String sentence;
        private String answer;
    }
}
