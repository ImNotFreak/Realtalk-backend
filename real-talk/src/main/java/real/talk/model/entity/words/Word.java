package real.talk.model.entity.words;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.User;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "words")
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Size(max = 255)
    @NotNull
    @Column(name = "term", nullable = false)
    private String term;

    @Column(name = "quote", length = Integer.MAX_VALUE)
    private String quote;

    @Size(max = 255)
    @NotNull
    @Column(name = "translation", nullable = false)
    private String translation;

    @Size(max = 255)
    @NotNull
    @Column(name = "translated_explanation", nullable = false)
    private String translatedExplanation;

    @Column(name = "another_example", length = Integer.MAX_VALUE)
    private String anotherExample;

    @Column(name = "time_code")
    private Double timeCode;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}