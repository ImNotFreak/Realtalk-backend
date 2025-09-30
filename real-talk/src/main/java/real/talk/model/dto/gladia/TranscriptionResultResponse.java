package real.talk.model.dto.gladia;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResultResponse {

    private String id;
    private String status;
    private Result result;
    private File file;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class File{
        private String id;
        private String filename;
        private String source;
        @JsonProperty("audio_duration")
        private Double audioDuration;
        @JsonProperty("number_of_channels")
        private Integer numberOfChannels;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private Transcription transcription;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transcription {
        @JsonProperty("full_transcript")
        private String fullTranscript;
        private List<Utterance> utterances;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Utterance {
        private String text;
        private double start;
        private double end;
        //TODO  Вероятно не нужен этот список слов, возможно удалим
        //private List<Word> words;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Word {
        private String word;
        private double start;
        private double end;
    }
}
