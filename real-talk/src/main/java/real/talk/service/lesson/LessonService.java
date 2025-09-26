package real.talk.service.lesson;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import real.talk.model.dto.lesson.LessonCreateRequest;
import real.talk.model.dto.lesson.LessonFilter;
import real.talk.model.dto.lesson.LessonGeneratedByLlm;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.User;
import real.talk.model.entity.enums.LessonAccess;
import real.talk.model.entity.enums.LessonStatus;
import real.talk.repository.lesson.LessonRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    public List<Lesson> createLessons(User user , LessonCreateRequest lessonRequest) {

        return lessonRequest.getYoutubeLinks().stream().map(youtubeLink -> {
            Lesson lesson = new Lesson();
            lesson.setId(UUID.randomUUID());
            lesson.setUser(user);
            lesson.setLanguage(lessonRequest.getLanguage());
            lesson.setLanguageLevel(lessonRequest.getLanguageLevel());
            lesson.setGrammarTopics(lessonRequest.getGrammarTopics());
            lesson.setYoutubeUrl(youtubeLink);
            lesson.setStatus(LessonStatus.PENDING);
            lesson.setAccess(LessonAccess.PUBLIC);
            lesson.setCreatedAt(Instant.now());
            lessonRepository.save(lesson);
            return lesson;
        }).toList();
    }

    public List<Lesson> getPendingLessons() {
        return lessonRepository.findByStatus(LessonStatus.PENDING);
    }

    public List<Lesson> getProcessingLessons() {
        return lessonRepository.findByStatus(LessonStatus.PROCESSING);
    }

    public List<Lesson> getLessonsWithGladiaDone() {
        return lessonRepository.findProcessingLessonsWithGladiaDone();
    }

    public List<Lesson> getLessonsWithLlmDone() {
        return lessonRepository.findProcessingLessonsWithLlmDone();
    }

    public List<LessonGeneratedByLlm> getPublicReadyLessons() {
        return lessonRepository.findByStatusAndAccess(LessonStatus.READY, LessonAccess.PUBLIC)
                .stream().map(lesson -> {
                    LessonGeneratedByLlm lessonData = lesson.getData();
                    lessonData.setYou_tube_url(lesson.getYoutubeUrl());
                    return lessonData;
                }).toList();
    }
/** Перегрузка с фильтрами — фильтрация выполняется в БД */
public List<LessonGeneratedByLlm> getPublicReadyLessons(LessonFilter f) {
    var list = lessonRepository.findPublicReadyFiltered(
            LessonStatus.READY.name(), LessonAccess.PUBLIC.name(),
            f.getLanguage(), f.getLanguageLevel(), f.getLessonTopic(), f.getGrammarContains()
    );
    return list.stream()
            .map(lesson -> {
                LessonGeneratedByLlm dto = lesson.getData();
                dto.setYou_tube_url(lesson.getYoutubeUrl());
                return dto;
            })
            .toList();
}



    public Lesson saveLesson(Lesson  lesson) {
        return lessonRepository.save(lesson);
    }


}
