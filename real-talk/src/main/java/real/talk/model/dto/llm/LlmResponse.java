package real.talk.model.dto.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LlmResponse {
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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Exercise {
        private String type;
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
