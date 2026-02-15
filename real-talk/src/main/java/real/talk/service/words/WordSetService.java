package real.talk.service.words;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import real.talk.model.dto.words.WordResponse;
import real.talk.model.dto.words.WordSetResponse;
import real.talk.model.entity.User;
import real.talk.model.entity.words.Word;
import real.talk.model.entity.words.WordSet;
import real.talk.model.entity.words.WordSetWord;
import real.talk.repository.user.UserRepository;
import real.talk.repository.words.WordRepository;
import real.talk.repository.words.WordSetRepository;
import real.talk.repository.words.WordSetWordRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WordSetService {
    private final WordSetRepository wordSetRepository;
    private final WordRepository wordRepository;
    private final WordSetWordRepository wordSetWordRepository;
    private final UserRepository userRepository;

    public WordSetResponse createWordSet(UUID userId, String name) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        WordSet set = new WordSet();
        set.setName(name);
        set.setArchived(false);
        set.setUser(user);
        wordSetRepository.save(set);

        return WordSetResponse.builder()
                .id(set.getId())
                .name(set.getName())
                .wordCount(set.getWordCount())
                .build();
    }

    @Transactional
    public void addWordsToSet(UUID wordSetId, List<UUID> wordIds) {
        WordSet wordSet = wordSetRepository.findById(wordSetId)
                .orElseThrow(() -> new RuntimeException("WordSet not found"));

        // Берём максимальную позицию среди существующих WordSetWord
        int maxPosition = wordSet.getWordSetWords().stream()
                .map(wsWord -> wsWord.getPosition() == null ? 0 : wsWord.getPosition())
                .max(Integer::compareTo)
                .orElse(0);

        for (int i = 0; i < wordIds.size(); i++) {
            UUID wordId = wordIds.get(i);

            // проверяем, есть ли уже слово в сете
            boolean exists = wordSet.getWordSetWords().stream()
                    .anyMatch(wsWord -> wsWord.getWord().getId().equals(wordId));
            if (exists)
                continue;

            Word word = wordRepository.findById(wordId)
                    .orElseThrow(() -> new RuntimeException("Word not found"));

            WordSetWord wsWord = new WordSetWord();
            wsWord.setWordSet(wordSet);
            wsWord.setWord(word);
            wsWord.setPosition(maxPosition + i + 1);

            wordSet.getWordSetWords().add(wsWord);
        }
        wordSet.updateWordCount();

        wordSetRepository.save(wordSet);
    }

    @Transactional(readOnly = true)
    public List<WordSetResponse> getWordSetsForUser(UUID userId) {
        List<WordSet> sets = wordSetRepository.findByUserUserId(userId);

        // Обновляем количество слов для каждого сета
        return sets.stream().map(set -> {
            set.updateWordCount();
            return WordSetResponse.builder()
                    .id(set.getId())
                    .name(set.getName())
                    .wordCount(set.getWordCount())
                    .build();
        }).toList();
    }

    @Transactional
    public void removeWordsFromSet(UUID wordSetId, List<UUID> wordIds) {
        wordSetWordRepository.deleteByWordSetIdAndWordIdIn(wordSetId, wordIds);
    }

    public void deleteWordSet(UUID wordSetId) {
        wordSetRepository.deleteById(wordSetId);
    }

    @Transactional(readOnly = true)
    public List<WordResponse> getWordsInSet(UUID wordSetId) {
        return wordSetWordRepository.findByWordSetIdOrderByPositionAsc(wordSetId)
                .stream()
                .map(wordSetWord -> {
                    Word word = wordSetWord.getWord();
                    return new WordResponse(
                            word.getId(),
                            word.getLesson().getId(),
                            word.getTerm(),
                            word.getQuote(),
                            word.getTranslation(),
                            word.getTranslatedExplanation(),
                            word.getAnotherExample(),
                            word.getTimeCode());
                })
                .toList();
    }

    public WordSetResponse renameWordSet(UUID wordSetId, String newName) {
        WordSet set = wordSetRepository.findById(wordSetId)
                .orElseThrow(() -> new RuntimeException("WordSet not found"));
        set.setName(newName);
        wordSetRepository.save(set);

        return WordSetResponse.builder()
                .id(set.getId())
                .name(set.getName())
                .wordCount(set.getWordCount())
                .build();
    }

    @Transactional(readOnly = true)
    public long getTotalSets(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return wordSetRepository.countByUser(user);
    }

    @Transactional
    public void shareWordSet(UUID wordSetId, UUID studentId) {
        WordSet wordSet = wordSetRepository.findById(wordSetId)
                .orElseThrow(() -> new RuntimeException("WordSet not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        wordSet.getSharedWith().add(student);
        wordSetRepository.save(wordSet);
    }

    @Transactional(readOnly = true)
    public List<WordSetResponse> getSharedWordSets(UUID studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        return wordSetRepository.findBySharedWithContains(student).stream()
                .map(set -> {
                    set.updateWordCount();
                    return WordSetResponse.builder()
                            .id(set.getId())
                            .name(set.getName())
                            .wordCount(set.getWordCount())
                            .build();
                })
                .toList();
    }
}
