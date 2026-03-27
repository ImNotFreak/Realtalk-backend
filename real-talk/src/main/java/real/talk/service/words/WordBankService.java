package real.talk.service.words;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import real.talk.model.dto.lesson.WordBankResponse;
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

        List<WordBankResponse.GlossaryItem> wordsInLesson = lesson.getData() != null
                && lesson.getData().getWordBankResponse() != null
                && lesson.getData().getWordBankResponse().getItems() != null
                        ? lesson.getData().getWordBankResponse().getItems()
                        : List.of();

        List<Word> newWords = wordsInLesson.stream()
                .filter(lessonWord -> lessonWord.getTimestamp() != null && !lessonWord.getTimestamp().isEmpty())
                .filter(lessonWord -> hasText(lessonWord.getExpression()))
                .filter(lessonWord -> hasText(lessonWord.getMeaning()))
                .map(lessonWord -> {
                    Word word = new Word();
                    word.setUser(user);
                    word.setLesson(lesson);
                    String normalizedMeaning = trimTo255(lessonWord.getMeaning());
                    word.setTerm(trimTo255(lessonWord.getExpression()));
                    word.setQuote(lessonWord.getQuote());
                    word.setTranslation(null);
                    word.setTranslatedExplanation(normalizedMeaning);
                    word.setTimeCode(parseTimeCode(lessonWord.getTimestamp()));
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
                        word.getLesson() != null ? word.getLesson().getId() : null,
                        word.getTerm(),
                        word.getQuote(),
                        word.getTranslatedExplanation(),
                        word.getTranslation(),
                        word.getTranslatedExplanation(),
                        word.getAnotherExample(),
                        word.getTimeCode()
                ));
    }

    private Double parseTimeCode(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return null;
        try {
            String[] parts = timestamp.split(":");
            if (parts.length == 2) {
                return Double.parseDouble(parts[0]) * 60 + Double.parseDouble(parts[1]);
            } else if (parts.length == 3) {
                return Double.parseDouble(parts[0]) * 3600 + Double.parseDouble(parts[1]) * 60 + Double.parseDouble(parts[2]);
            }
            return Double.parseDouble(timestamp);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimTo255(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() <= 255) {
            return normalized;
        }
        return normalized.substring(0, 255);
    }
}
