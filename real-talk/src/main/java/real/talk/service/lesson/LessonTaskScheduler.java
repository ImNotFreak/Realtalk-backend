package real.talk.service.lesson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import real.talk.model.dto.gladia.TranscriptionResultResponse;
import real.talk.model.dto.lesson.LessonGeneratedByLlm;
import real.talk.model.entity.GladiaData;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.LlmData;
import real.talk.model.entity.User;
import real.talk.model.entity.enums.LessonStatus;
import real.talk.service.llm.LlmDataService;
import real.talk.service.transcription.GladiaService;
import real.talk.service.user.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonTaskScheduler {

    private final LlmDataService llmDataService;
    private final LessonService lessonService;
    private final GladiaService gladiaService;
    private final UserService userService;

    @Scheduled(cron = "${lesson.ready-lesson.cron}")
    @Transactional
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

            lesson.setLessonTopic(lessonData.getLesson_theme());
            lesson.setData(lessonData);
            lesson.setStatus(LessonStatus.READY);
            lessonService.saveLesson(lesson);
            log.info("✅ Урок id={} обновлен и сохранен со статусом READY", lesson.getId());

            User user = lesson.getUser();
            user.setDuration(user.getDuration() + gladia.getData().getFile().getAudioDuration());
            user.setLessonCount(user.getLessonCount() + 1);
            userService.saveUser(user);

            log.info("User [{}] updated: duration -> {}, lessons -> {}",
                    user.getUserId(),
                    user.getDuration(),
                    user.getLessonCount()
            );
        });
    }

    private void setGlossaryTimeCode(List<LessonGeneratedByLlm.GlossaryItem> glossary,
                                     List<TranscriptionResultResponse.Utterance> utterances) {

        LevenshteinDistance levenshtein = new LevenshteinDistance();

        for (LessonGeneratedByLlm.GlossaryItem item : glossary) {
            String quote = item.getQuote();
            boolean matched = false;

            // 🔹 1. Пробуем точное вхождение
            for (TranscriptionResultResponse.Utterance utterance : utterances) {
                if (utterance.getText().contains(quote) || quote.contains(utterance.getText())) {
                    item.setTimeCode(Math.floor(utterance.getStart()));
                    log.info("✅ Exact match: \"{}\" ↔ \"{}\" (time={})",
                            quote, utterance.getText(), utterance.getStart());
                    matched = true;
                    break;
                }
            }

            // 🔹 2. Если не нашли — fuzzy matching
            if (!matched) {
                TranscriptionResultResponse.Utterance bestMatch = null;
                int bestDistance = Integer.MAX_VALUE;

                for (TranscriptionResultResponse.Utterance utterance : utterances) {
                    int distance = levenshtein.apply(quote, utterance.getText());
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestMatch = utterance;
                    }
                }

                if (bestMatch != null) {
                    double similarity = 1 - (double) bestDistance /
                            Math.max(quote.length(), bestMatch.getText().length());

                    if (similarity >= 0.6) {
                        item.setTimeCode(Math.floor(bestMatch.getStart()));
                        log.info("🤝 Fuzzy match: \"{}\" ↔ \"{}\" (similarity={}%, time={})",
                                quote, bestMatch.getText(),
                                String.format("%.2f", similarity * 100),
                                bestMatch.getStart());
                    } else {
                        log.warn("❌ No reliable match for \"{}\". Best candidate: \"{}\" (similarity={}%)",
                                quote,
                                bestMatch.getText(),
                                String.format("%.2f", similarity * 100));
                    }
                }
            }
        }
    }
}
