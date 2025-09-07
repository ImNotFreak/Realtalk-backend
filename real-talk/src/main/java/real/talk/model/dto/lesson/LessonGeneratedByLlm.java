package real.talk.model.dto.lesson;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonGeneratedByLlm {
    private String youTubeUrl;
    private List<GlossaryItem> glossary;
    private List<Exercise> lexicalExercises;
    private List<Exercise> grammarExercises;
    private List<String> quizlet;
    private Answers answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlossaryItem {
        private String expression;
        private String quote;
        private String translation;
        private String explanation;
        private String example;
        private Double timeCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Exercise {
        private String type;
        private String input;
        private List<String> examples;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answers {
        private List<String> lexicalExercises;
        private List<String> grammarExercises;
    }
}
