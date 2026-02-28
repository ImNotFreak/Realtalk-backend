package real.talk.service.history;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import real.talk.model.dto.history.LessonHistoryResponse;
import real.talk.model.entity.enums.LessonAccess;
import real.talk.model.entity.enums.LessonStatus;
import real.talk.repository.history.UserLessonHistoryRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LessonHistoryService {

    private final UserLessonHistoryRepository historyRepository;
    public void trackLessonOpen(UUID userId, UUID lessonId) {
        historyRepository.upsertLessonHistory(userId, lessonId, Instant.now());
    }

    @Transactional(readOnly = true)
    public Map<String, List<LessonHistoryResponse>> getHistory(UUID userId,
                                                               LessonStatus status,
                                                               LessonAccess access,
                                                               Pageable pageable) {
        Page<LessonHistoryResponse> lessons =
                historyRepository.getLessonsWithHistory(status, access, userId, pageable);

        Map<String, List<LessonHistoryResponse>> grouped = new HashMap<>();
        grouped.put("recent", new ArrayList<>());
        grouped.put("lastWeek", new ArrayList<>());
        grouped.put("thisMonth", new ArrayList<>());

        Instant now = Instant.now();
        for (LessonHistoryResponse lesson : lessons) {
            Instant openedAt = lesson.getLastOpenedAt();
            if (openedAt == null) continue; // можно игнорировать или добавлять в отдельный список

            if (openedAt.isAfter(now.minus(1, ChronoUnit.MINUTES))) {
                grouped.get("recent").add(lesson);
            } else if (openedAt.isAfter(now.minus(7, ChronoUnit.DAYS))) {
                grouped.get("lastWeek").add(lesson);
            } else if (openedAt.isAfter(now.minus(14, ChronoUnit.DAYS))) {
                grouped.get("thisMonth").add(lesson);
            }
        }

        return grouped;
    }

    @Transactional(readOnly = true)
    public long getTotalHistory(UUID userId) {
        return historyRepository.countUserLessonHistoriesByUserId(userId);
    }
}
