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

import java.util.List;

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

        if (pendingLessons == null || pendingLessons.isEmpty()) return;

        pendingLessons.forEach(lesson -> {
            log.info("Processing lesson {}", lesson);
            lesson.setStatus(LessonStatus.PROCESSING);
            PreRecorderResponse preRecorderResponse = transcriptionService.transcribeAudio(lesson.getYoutubeUrl());
            GladiaData gladiaData = new GladiaData();
            gladiaData.setLesson(lesson);
            gladiaData.setStatus(DataStatus.CREATED);
            gladiaData.setGladiaRequestId(preRecorderResponse.getId());
            gladiaService.saveGladiaData(gladiaData);
            lessonService.saveLesson(lesson);
            log.info("Finished processing lesson {}", lesson);
        });
    }

    @Scheduled(cron = "${gladia.get-response.cron}")
    public void processGladiaResponse() {
        List<GladiaData> gladiaDataByStatus = gladiaService.getGladiaDataByStatusCreated();

        if (gladiaDataByStatus == null || gladiaDataByStatus.isEmpty()) return;

        gladiaDataByStatus.forEach(gladiaData -> {
            log.info("Processing gladiaRequest {}", gladiaData.getGladiaRequestId());
            TranscriptionResultResponse transcriptionResult = transcriptionService.getTranscriptionResult(gladiaData.getGladiaRequestId());
            if (transcriptionResult.getStatus().equals("done")) {
                gladiaData.setStatus(DataStatus.DONE);
                gladiaData.setData(transcriptionResult);
                gladiaService.saveGladiaData(gladiaData);
                log.info("Finished processing gladiaRequest {}", gladiaData);
            }
        });
    }
}
