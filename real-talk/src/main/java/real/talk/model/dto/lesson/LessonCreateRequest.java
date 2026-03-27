package real.talk.model.dto.lesson;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonCreateRequest {
    private String name;

    @JsonProperty("youtube_link")
    @JsonAlias("youtubeLink")
    private String youtubeLink;

    @JsonProperty("segment_start_min")
    @JsonAlias("segmentStartMin")
    private Double segmentStartMin;

    @JsonProperty("segment_end_min")
    @JsonAlias("segmentEndMin")
    private Double segmentEndMin;

    private String email;
    private String language;

    @JsonProperty("language_level")
    @JsonAlias("languageLevel")
    private String languageLevel;

    @JsonProperty("grammar_topics")
    @JsonAlias("grammarTopics")
    private List<String> grammarTopics;

    private String preset;
}
