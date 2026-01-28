package real.talk.service.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import real.talk.repository.history.UserLessonHistoryRepository;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLessonHistoryCleanupService {

    private final UserLessonHistoryRepository historyRepository;
    @Value("${history.cleanup.retention-days}")
    private int retentionDays;

    /**
     * Удаляем все записи старше 30 дней
     */
    @Transactional
    @Scheduled(cron = "${history.cleanup.cron}") // каждый день в 02:00 ночи
    public void cleanupOldHistory() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));
        int deleted = historyRepository.deleteOlderThan(cutoff);
        log.info("UserLessonHistory cleanup: deleted {} old records", deleted);
    }
}
