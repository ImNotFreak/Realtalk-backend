package real.talk.repository.lesson;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import real.talk.model.dto.lesson.LessonLiteResponse;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.enums.LessonAccess;
import real.talk.model.entity.enums.LessonStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
        Optional<Lesson> findById(UUID lessonId);

        List<Lesson> findByStatus(LessonStatus lessonStatus);

        List<Lesson> findByStatusAndAccess(LessonStatus lessonStatus, LessonAccess lessonAccess);

        @Query(value = """
                        SELECT l.* FROM lessons l
                        JOIN gladia_data g ON l.lesson_id = g.lesson_id
                        LEFT JOIN llm_data llm ON l.lesson_id = llm.lesson_id
                        WHERE g.status = 'DONE' AND llm.lesson_id IS NULL
                        ORDER BY g.created_at ASC
                        LIMIT 1""", nativeQuery = true)
        Optional<Lesson> findProcessingLessonWithGladiaDone();

        @Query(value = """
                        SELECT l.* FROM lessons l
                        JOIN gladia_data g ON l.lesson_id = g.lesson_id
                        LEFT JOIN llm_data llm ON l.lesson_id = llm.lesson_id
                        WHERE g.status = 'DONE' AND llm.lesson_id IS NULL
                        ORDER BY g.created_at ASC
                        LIMIT :limit""", nativeQuery = true)
        List<Lesson> findBatchProcessingLessonsWithGladiaDone(@Param("limit") int limit);

        @Query(value = """
                        SELECT l.* FROM lessons l
                        JOIN llm_data llm ON l.lesson_id = llm.lesson_id
                        WHERE l.status != 'READY' AND llm.status = 'DONE'""", nativeQuery = true)
        List<Lesson> findProcessingLessonsWithLlmDone();

        // --- Новая версия с пагинацией и безопасной сортировкой (через токены s1,s2)
        // ---
        @Query(value = """
                        SELECT l.*
                        FROM lessons l
                        WHERE l.status = CAST(:status AS varchar)
                        AND l.access = CAST(:access AS varchar)
                        AND (:language IS NULL OR LOWER(TRIM(l.language)) = LOWER(TRIM(:language)))
                        AND (:level    IS NULL OR LOWER(TRIM(l.language_level)) = LOWER(TRIM(:level)))
                        AND (:topic    IS NULL OR LOWER(l.lesson_topic) LIKE LOWER(CONCAT('%', :topic, '%')))
                        AND (:grammar  IS NULL OR LOWER(array_to_string(l.grammar_topics, ', ')) LIKE LOWER(CONCAT('%', :grammar, '%')))
                        ORDER BY
                        /* s1 */
                        CASE WHEN :s1 = 'language_asc'      THEN l.language      END ASC,
                        CASE WHEN :s1 = 'language_desc'     THEN l.language      END DESC,
                        CASE WHEN :s1 = 'lesson_topic_asc'  THEN l.lesson_topic  END ASC,
                        CASE WHEN :s1 = 'lesson_topic_desc' THEN l.lesson_topic  END DESC,
                        CASE WHEN :s1 = 'created_at_asc'    THEN l.created_at    END ASC,
                        CASE WHEN :s1 = 'created_at_desc'   THEN l.created_at    END DESC,
                        /* s2 */
                        CASE WHEN :s2 = 'language_asc'      THEN l.language      END ASC,
                        CASE WHEN :s2 = 'language_desc'     THEN l.language      END DESC,
                        CASE WHEN :s2 = 'lesson_topic_asc'  THEN l.lesson_topic  END ASC,
                        CASE WHEN :s2 = 'lesson_topic_desc' THEN l.lesson_topic  END DESC,
                        CASE WHEN :s2 = 'created_at_asc'    THEN l.created_at    END ASC,
                        CASE WHEN :s2 = 'created_at_desc'   THEN l.created_at    END DESC,
                        /* fallback чтобы порядок был стабильным */
                        l.created_at DESC""", countQuery = """
                        SELECT COUNT(*)
                        FROM lessons l
                        WHERE l.status = CAST(:status AS varchar)
                        AND l.access = CAST(:access AS varchar)
                        AND (:language IS NULL OR LOWER(TRIM(l.language)) = LOWER(TRIM(:language)))
                        AND (:level    IS NULL OR LOWER(TRIM(l.language_level)) = LOWER(TRIM(:level)))
                        AND (:topic    IS NULL OR LOWER(l.lesson_topic) LIKE LOWER(CONCAT('%', :topic, '%')))
                        AND (:grammar  IS NULL OR LOWER(array_to_string(l.grammar_topics, ', ')) LIKE LOWER(CONCAT('%', :grammar, '%')))""", nativeQuery = true)
        Page<Lesson> findPublicReadyFilteredPaged(@Param("status") String status,
                        @Param("access") String access,
                        @Param("language") String language,
                        @Param("level") String languageLevel,
                        @Param("topic") String lessonTopic,
                        @Param("grammar") String grammarContains,
                        @Param("s1") String sortToken1,
                        @Param("s2") String sortToken2,
                        Pageable pageable);

        Boolean existsByIdAndStatusEquals(UUID id, LessonStatus status);

        /**
         * Лёгкий список уроков без поля data (JSON): выбираем только нужные колонки
         * через JPQL-конструктор.
         */
        @Query("""
                        select new real.talk.model.dto.lesson.LessonLiteResponse(
                            l.id,
                            l.youtubeUrl,
                            l.lessonTopic,
                            l.status,
                            l.tags,
                            l.createdAt
                        )
                        from Lesson l
                        where l.status = :status
                          and l.access = :access
                        """)
        Page<LessonLiteResponse> findLiteByStatusAndAccess(
                        @Param("status") LessonStatus status,
                        @Param("access") LessonAccess access,
                        Pageable pageable);

        @Query("""
                        select new real.talk.model.dto.lesson.LessonLiteResponse(
                            l.id,
                            l.youtubeUrl,
                            l.lessonTopic,
                            l.status,
                            l.tags,
                            l.createdAt
                        )
                        from Lesson l
                        join l.user u
                        where l.status in :statuses
                          and l.access = :access
                          and u.email = :email
                        """)
        Page<LessonLiteResponse> findLiteByStatusAndAccessAndEmail(
                        @Param("statuses") Collection<LessonStatus> statuses,
                        @Param("access") LessonAccess access,
                        @Param("email") String email,
                        Pageable pageable);

        @Query("""
                        select new real.talk.model.dto.lesson.LessonLiteResponse(
                            l.id,
                            l.youtubeUrl,
                            l.lessonTopic,
                            l.status,
                            l.tags,
                            l.createdAt
                        )
                        from Lesson l
                        join l.user u
                        where l.status = :status
                          and l.access = :access
                          and u.role = :role
                        """)
        Page<LessonLiteResponse> findLiteByStatusAndAccessAndUserRole(
                        @Param("status") LessonStatus status,
                        @Param("access") LessonAccess access,
                        @Param("role") real.talk.model.entity.enums.UserRole role,
                        Pageable pageable);

        @Query("""
                        select new real.talk.model.dto.lesson.LessonLiteResponse(
                            l.id,
                            l.youtubeUrl,
                            l.lessonTopic,
                            l.status,
                            l.tags,
                            l.createdAt
                        )
                        from Lesson l
                        join l.sharedUsers u
                        where u.userId = :userId
                        """)
        List<LessonLiteResponse> findSharedLessons(@Param("userId") UUID userId);
}
