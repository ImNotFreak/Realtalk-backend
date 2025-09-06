package real.talk.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import real.talk.model.dto.llm.LlmResponse;
import real.talk.model.entity.enums.DataStatus;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "llm_data")
public class LlmData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private DataStatus status;

    @Column(name = "data")
    @JdbcTypeCode(SqlTypes.JSON)
    private LlmResponse data;

}