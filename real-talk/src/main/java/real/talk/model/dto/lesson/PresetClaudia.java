package real.talk.model.dto.lesson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PresetClaudia {

    @JsonProperty("warm_up")
    private WarmUp warmUp;

    @JsonProperty("vocabulary_unlock")
    private VocabularyUnlock vocabularyUnlock;

    @JsonProperty("big_picture")
    private BigPicture bigPicture;

    @JsonProperty("fact_check")
    private FactCheck factCheck;

    @JsonProperty("grammar_note")
    private GrammarNote grammarNote;

    @JsonProperty("make_the_right_choice")
    private MakeTheRightChoice makeTheRightChoice;

    @JsonProperty("fix_it")
    private FixIt fixIt;

    @JsonProperty("real_talk")
    private RealTalk realTalk;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WarmUp {
        private List<String> options;
        private String correct_answer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VocabularyUnlock {
        private List<String> terms;
        private List<String> meanings;
        private List<List<String>> answer_pairs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BigPicture {
        private List<String> options;
        private String correct_answer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactCheck {
        private List<String> statements;
        private List<Boolean> answers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GrammarNote {
        private String example;
        private String grammar_rule;
        private String contrast_or_common_mistake;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MakeTheRightChoice {
        private String title;
        private String instruction;
        private List<Item> items;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Item {
            private String sentence;
            private List<String> options;
            private String correct_answer;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FixIt {
        private String title;
        private String instruction;
        private List<Item> items;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Item {
            private String incorrect;
            private String correct;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RealTalk {
        private String title;
        private String instruction;
        private List<String> questions;
    }
}
