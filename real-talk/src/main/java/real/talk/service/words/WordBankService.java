package real.talk.service.words;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import real.talk.model.dto.lesson.LessonGeneratedByLlm;
import real.talk.model.dto.words.WordResponse;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.words.Word;
import real.talk.repository.lesson.LessonRepository;
import real.talk.repository.user.UserRepository;
import real.talk.repository.words.WordRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WordBankService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final WordRepository wordRepository;

    @Transactional
    public void addLessonWordsToUser(UUID userId, UUID lessonId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        boolean exists = wordRepository.existsByUserUserIdAndLessonId(userId, lessonId);
        if (exists) {
            return; // слова уже есть — ничего не делаем
        }

        List<LessonGeneratedByLlm.GlossaryItem> wordsInLesson = lesson.getData().getGlossary();

        List<Word> newWords = wordsInLesson.stream()
                .filter(lessonWord -> lessonWord.getTimeCode() != null)
                .map(lessonWord -> {
                    Word word = new Word();
                    word.setUser(user);
                    word.setLesson(lesson);
                    word.setTerm(lessonWord.getTerm());
                    word.setQuote(lessonWord.getQuote());
                    word.setTranslation(lessonWord.getTranslation_ru());
                    word.setTranslatedExplanation(lessonWord.getExplanation_ru());
                    word.setTimeCode(lessonWord.getTimeCode());
                    word.setAnotherExample(lessonWord.getAnother_example());
                    return word;
                })
                .toList();

        wordRepository.saveAll(newWords);
    }

    @Transactional(readOnly = true)
    public Page<WordResponse> getAllWordsForUser(UUID userId, Pageable pageable) {
        return wordRepository.findByUserUserId(userId, pageable)
                .map(word -> new WordResponse(
                        word.getId(),
                        word.getLesson().getId(),
                        word.getTerm(),
                        word.getQuote(),
                        word.getTranslation(),
                        word.getTranslatedExplanation(),
                        word.getAnotherExample(),
                        word.getTimeCode()
                ));
    }
}
