package real.talk.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import real.talk.model.dto.gladia.TranscriptionResultResponse;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "gladia_request_id")
    private UUID gladiaRequestId;

    @Column(name = "gladia_full_url")
    private String gladiaFullUrl;

    @Column(name = "data")
    @JdbcTypeCode(SqlTypes.JSON)
    private TranscriptionResultResponse data;

}