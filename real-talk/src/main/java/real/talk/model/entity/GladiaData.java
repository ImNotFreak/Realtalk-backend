package real.talk.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import real.talk.model.dto.gladia.TranscriptionResultResponse;
import real.talk.model.entity.enums.DataStatus;
import real.talk.model.entity.enums.LessonStatus;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "gladia_data")
public class GladiaData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "gladia_request_id")
    private UUID gladiaRequestId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private DataStatus status;

    @Column(name = "data")
    @JdbcTypeCode(SqlTypes.JSON)
    private TranscriptionResultResponse data;

}