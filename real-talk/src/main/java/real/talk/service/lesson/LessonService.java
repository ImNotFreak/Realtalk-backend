package real.talk.service.lesson;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.ArrayList;

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

    //Перегрузка с фильтрами — теперь постранично + сортировка (белый список) */
    public Page<LessonGeneratedByLlm> getPublicReadyLessons(LessonFilter f) {
        Pageable pageable = buildPageable(f.getPage(), f.getSize()); // только page/size
        // разберём sort=language,-lesson_topic → s1,s2 токены
        String[] sortTokens = toSortTokens(f.getSort());
        Page<Lesson> page = lessonRepository.findPublicReadyFilteredPaged(
                LessonStatus.READY.name(), LessonAccess.PUBLIC.name(),
                f.getLanguage(), f.getLanguageLevel(), f.getLessonTopic(), f.getGrammarContains(),
                sortTokens[0], sortTokens[1],
                pageable
        );
        return page.map(lesson -> {
            LessonGeneratedByLlm dto = lesson.getData();
            dto.setYou_tube_url(lesson.getYoutubeUrl());
            return dto;
            });
        }

        // ===== helpers =====
    private Pageable buildPageable(Integer page, Integer size) {
                int p = (page == null || page < 0) ? 0 : page;
                int s = (size == null || size <= 0) ? 10 : Math.min(size, 50);
                return PageRequest.of(p, s);
        }

        /**
 + * Преобразуем sort-параметр в 2 безопасных токена для SQL:
 + * input: "language,-lesson_topic" → ["language_asc","lesson_topic_desc"]
 + * Белый список: language | lesson_topic | createdAt
 + * Любой мусор → игнор → дефолт "created_at_desc".
 + */
        private String[] toSortTokens(String sortParam) {
        String def = "created_at_desc";
        String s1 = def, s2 = def;
        if (sortParam == null || sortParam.isBlank()) return new String[]{s1, s2};

                ArrayList<String> tokens = new ArrayList<>();
        for (String raw : sortParam.split(",")) {
            String t = raw.trim();
            if (t.isEmpty()) continue;
            boolean desc = t.startsWith("-");
            String key = t.replaceFirst("^[+-]", "");
            String dbKey = switch (key) {
                case "language"      -> "language";
                case "lesson_topic"  -> "lesson_topic";
                case "createdAt"     -> "created_at";
                default -> null; // не из белого списка — пропускаем
                };
            if (dbKey != null) {
                tokens.add(dbKey + (desc ? "_desc" : "_asc"));
                }
            }
        if (!tokens.isEmpty()) s1 = tokens.get(0);
        if (tokens.size() > 1) s2 = tokens.get(1);
        return new String[]{s1, s2};
        }



    public Lesson saveLesson(Lesson  lesson) {
        return lessonRepository.save(lesson);
    }


}
