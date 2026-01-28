package real.talk.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "user_lesson_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lesson_id"})
)
public class UserLessonHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @NotNull
    @Column(name = "last_opened_at", nullable = false)
    private Instant lastOpenedAt;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "open_count", nullable = false)
    private Integer openCount;

    public void touch() {
        this.lastOpenedAt = Instant.now();
        this.openCount++;
    }
}