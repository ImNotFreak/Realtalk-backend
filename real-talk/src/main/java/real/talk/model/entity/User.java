package real.talk.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import real.talk.model.entity.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "submission_time")
    private Instant submissionTime;

    @Column(name = "name")
    private String name;

    @Column(name = "order_number")
    private UUID orderNumber;

    @Email
    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "telegram")
    private String telegram;

    @Column(name = "role")
    private UserRole role;

    @Column(name = "lesson_count")
    private Integer lessonCount;

    @Column(name = "duration")
    private Double duration;
}