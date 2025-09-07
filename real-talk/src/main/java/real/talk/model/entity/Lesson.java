package real.talk.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import real.talk.model.dto.lesson.LessonGeneratedByLlm;
import real.talk.model.entity.enums.LessonAccess;
import real.talk.model.entity.enums.LessonStatus;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    @Column(name = "lesson_id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 10)
    @Column(name = "language_level", length = 10)
    private String languageLevel;

    @Size(max = 255)
    @Column(name = "youtube_url")
    private String youtubeUrl;

    @Column(name = "grammar_topics")
    private List<String> grammarTopics;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LessonStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "access", nullable = false, length = 20)
    private LessonAccess access;

    @Column(name = "data")
    @JdbcTypeCode(SqlTypes.JSON)
    private LessonGeneratedByLlm data;
}