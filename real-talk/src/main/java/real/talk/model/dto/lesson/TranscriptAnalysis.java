package real.talk.model.dto.lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptAnalysis {
    private String main_idea;
    private String lexical_field;
    private List<KeyPoint> key_points;
    private List<CandidateVocabulary> candidate_vocabulary;
    private List<GrammarEvidence> grammar_evidence;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyPoint {
        private String point;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateVocabulary {
        private String expression;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrammarEvidence {
        private String text;
    }
}
