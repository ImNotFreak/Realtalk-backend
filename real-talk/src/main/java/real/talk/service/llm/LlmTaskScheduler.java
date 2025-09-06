package real.talk.service.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import real.talk.model.dto.llm.LlmResponse;
import real.talk.model.entity.GladiaData;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.LlmData;
import real.talk.model.entity.enums.DataStatus;
import real.talk.service.lesson.LessonService;
import real.talk.service.transcription.GladiaService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmTaskScheduler {


    private final LessonService lessonService;
    private final GladiaService gladiaService;
    private final GptLessonService gptLessonService;
    private final LlmDataService llmDataService;

    @Scheduled(cron = "${llm.generate-lesson.cron}")
    public void processPendingLessons(){
        List<Lesson> processingLessons = lessonService.getProcessingLessons();

        if (processingLessons == null || processingLessons.isEmpty()) return;

        processingLessons
                .forEach(lesson -> {
                    try {
                        if (llmDataService.existsByLessonIdAndStatusDone(lesson.getId())){
                            log.info("Урок уже сгенерирован {}", lesson.getId());
                            return;
                        }

                        log.info("Обрабатываем урок: id={}", lesson.getId());
                        GladiaData data = gladiaService.getGladiaDataByLessonIdAndStatusDone(lesson.getId())
                                .orElseThrow(() -> new RuntimeException("Gladia Data Not Generated yet for lesson " + lesson.getId()));


                        log.info("GladiaData найдено для урока id={}", lesson.getId());
                        LlmResponse generatedLesson = gptLessonService.createLesson(lesson, data);
                        log.info("Урок сгенерирован GPT для урока id={}", lesson.getId());

                        LlmData llmData = new LlmData();
                        llmData.setLesson(lesson);
                        llmData.setStatus(DataStatus.DONE);
                        llmData.setData(generatedLesson);
                        llmDataService.save(llmData);
                        log.info("Сгенерированные данные LLM сохранены для урока id={}", lesson.getId());

                } catch (Exception e) {
                        log.error("Ошибка при обработке урока id={}", lesson.getId(), e);
                    }
                });

    }
}
