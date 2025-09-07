package real.talk.service.lesson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import real.talk.model.dto.gladia.TranscriptionResultResponse;
import real.talk.model.dto.lesson.LessonGeneratedByLlm;
import real.talk.model.entity.GladiaData;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.LlmData;
import real.talk.model.entity.enums.LessonStatus;
import real.talk.service.llm.LlmDataService;
import real.talk.service.transcription.GladiaService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonTaskScheduler {

    private final LlmDataService llmDataService;
    private final LessonService lessonService;
    private final GladiaService gladiaService;

    @Scheduled(cron = "${lesson.ready-lesson.cron}")
    public void processReadyLessons(){
        List<Lesson> lessonsWithLlmDone = lessonService.getLessonsWithLlmDone();
        if(lessonsWithLlmDone == null || lessonsWithLlmDone.isEmpty()){
            log.info("Lessons with Llm Done not found");
            return;
        }

        lessonsWithLlmDone.forEach(lesson -> {
            log.info("▶️ Обработка урока id={} (статус={})", lesson.getId(), lesson.getStatus());
            GladiaData gladia = gladiaService.getGladiaDataByLessonIdAndStatusDone(lesson.getId())
                    .orElseThrow(() -> {
                        log.error("❌ GladiaData не найден для lessonId={}", lesson.getId());
                        return new RuntimeException("GladiaData not found");
                    });
            LlmData llm = llmDataService.getLlmDataByLessonIdAndStatusDone(lesson.getId())
                    .orElseThrow(() -> {
                        log.error("❌ LlmData не найден для lessonId={}", lesson.getId());
                        return new RuntimeException("LlmData not found");
                    });

            LessonGeneratedByLlm lessonData = llm.getData();
            List<TranscriptionResultResponse.Utterance> utterances = gladia.getData().getResult().getTranscription().getUtterances();
            List<LessonGeneratedByLlm.GlossaryItem> glossary = lessonData.getGlossary();

            log.info("📖 Урок id={} содержит {} элементов в glossary и {} utterances",
                    lesson.getId(), glossary.size(), utterances.size());

            setGlossaryTimeCode(glossary, utterances);
            lesson.setData(lessonData);
            lesson.setStatus(LessonStatus.READY);
            lessonService.saveLesson(lesson);
            log.info("✅ Урок id={} обновлен и сохранен со статусом READY", lesson.getId());
        });
    }

    private void setGlossaryTimeCode(List<LessonGeneratedByLlm.GlossaryItem> glossary, List<TranscriptionResultResponse.Utterance> utterances){

        Map<String, Double> timeCodes = utterances.stream()
                .collect(Collectors.toMap(
                        TranscriptionResultResponse.Utterance::getText,
                        TranscriptionResultResponse.Utterance::getStart,
                        (existing, replacement) -> existing
                ));

        for (LessonGeneratedByLlm.GlossaryItem item : glossary) {
            Double timeCode = timeCodes.get(item.getQuote());
            if (timeCode != null) {
                item.setTimeCode(timeCode);
            }
        }
    }
}
