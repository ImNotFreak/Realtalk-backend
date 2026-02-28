package real.talk.service.transcription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import real.talk.model.dto.gladia.PreRecorderResponse;
import real.talk.model.dto.gladia.TranscriptionResultResponse;
import real.talk.model.entity.GladiaData;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.enums.DataStatus;
import real.talk.model.entity.enums.LessonStatus;
import real.talk.service.lesson.LessonService;

import java.time.Instant;
import java.util.List;

import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;

@Service
@RequiredArgsConstructor
@Slf4j
public class GladiaTaskScheduler {

    private final TranscriptionService transcriptionService;
    private final GladiaService gladiaService;
    private final LessonService lessonService;

    @Scheduled(cron = "${gladia.create-request.cron}")
    public void processGladiaRequest() {
        List<Lesson> pendingLessons = lessonService.getPendingLessons();

        if (pendingLessons == null || pendingLessons.isEmpty())
            return;

        log.info("Starting concurrent gladia requests for {} lessons", pendingLessons.size());
        try (var executor = newVirtualThreadPerTaskExecutor()) {
            pendingLessons.forEach(lesson -> executor.submit(() -> processLessonRequest(lesson)));
        }
        log.info("Finished submitting gladia requests to executor");
    }

    private void processLessonRequest(Lesson lesson) {
        try {
            log.info("Processing lesson {}", lesson.getId());
            lesson.setStatus(LessonStatus.PROCESSING);
            PreRecorderResponse preRecorderResponse = transcriptionService.transcribeAudio(lesson.getYoutubeUrl());
            GladiaData gladiaData = new GladiaData();
            gladiaData.setLesson(lesson);
            gladiaData.setStatus(DataStatus.CREATED);
            gladiaData.setGladiaRequestId(preRecorderResponse.getId());
            gladiaData.setCreatedAt(Instant.now());
            gladiaService.saveGladiaData(gladiaData);
            lessonService.saveLesson(lesson);
            log.info("Finished processing lesson {}", lesson.getId());
        } catch (Exception e) {
            log.error("Error processing lesson {}", lesson.getId(), e);
        }
    }

    @Scheduled(cron = "${gladia.get-response.cron}")
    public void processGladiaResponse() {
        List<GladiaData> gladiaDataByStatus = gladiaService.getGladiaDataByStatusCreated();

        if (gladiaDataByStatus == null || gladiaDataByStatus.isEmpty())
            return;

        log.info("Starting concurrent gladia responses check for {} tasks", gladiaDataByStatus.size());
        try (var executor = newVirtualThreadPerTaskExecutor()) {
            gladiaDataByStatus.forEach(gladiaData -> executor.submit(() -> processGladiaDataResponse(gladiaData)));
        }
        log.info("Finished submitting gladia responses to executor");
    }

    private void processGladiaDataResponse(GladiaData gladiaData) {
        try {
            log.info("Processing gladiaRequest {}", gladiaData.getGladiaRequestId());
            TranscriptionResultResponse transcriptionResult = transcriptionService
                    .getTranscriptionResult(gladiaData.getGladiaRequestId());
            if (transcriptionResult.getStatus().equals("done")) {
                gladiaData.setStatus(DataStatus.DONE);
                gladiaData.setData(transcriptionResult);
                gladiaService.saveGladiaData(gladiaData);
                log.info("Finished processing gladiaRequest {}", gladiaData.getGladiaRequestId());
            }
        } catch (Exception e) {
            log.error("Error checking response for {}", gladiaData.getGladiaRequestId(), e);
        }
    }
}
