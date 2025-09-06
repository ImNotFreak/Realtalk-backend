package real.talk.service.lesson;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import real.talk.model.dto.lesson.LessonRequest;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.User;
import real.talk.model.entity.enums.LessonStatus;
import real.talk.repository.lesson.LessonRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    public List<Lesson> createLessons(User user , LessonRequest lessonRequest) {

        return lessonRequest.getYoutubeLinks().stream().map(youtubeLink -> {
            Lesson lesson = new Lesson();
            lesson.setId(UUID.randomUUID());
            lesson.setUser(user);
            lesson.setLanguageLevel(lessonRequest.getLanguageLevel());
            lesson.setGrammarTopics(lessonRequest.getGrammarTopics());
            lesson.setYoutubeUrl(youtubeLink);
            lesson.setStatus(LessonStatus.PENDING);
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

    public Lesson saveLesson(Lesson  lesson) {
        return lessonRepository.save(lesson);
    }
}
