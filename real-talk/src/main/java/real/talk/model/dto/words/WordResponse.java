package real.talk.model.dto.words;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.User;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordResponse {
    private UUID id;
    private UUID lessonId;
    private String term;
    private String quote;
    private String translation;
    private String translatedExplanation;
    private String anotherExample;
    private Double timeCode;
}
