package real.talk.model.dto.folder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record AddLessonToFolderRequest(
        @JsonProperty("lessonId")
        UUID lessonId
) {}
