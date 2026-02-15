package real.talk.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import real.talk.model.dto.words.WordResponse;
import real.talk.model.dto.words.WordSetResponse;
import real.talk.model.entity.User;
import real.talk.service.words.WordBankService;
import real.talk.service.words.WordSetService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/words")
@RequiredArgsConstructor
public class WordSetController {

    private final WordSetService wordSetService;
    private final WordBankService wordBankService;

    // -------------------- Word Bank --------------------

    /**
     * Добавляем слова урока в Word Bank пользователя.
     * Если слова уже есть для этого lessonId, ничего не делаем.
     */
    /**
     * Добавляем слова урока в Word Bank пользователя.
     * Если слова уже есть для этого lessonId, ничего не делаем.
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addLessonWords(@AuthenticationPrincipal User user,
            @RequestParam UUID lessonId) {
        wordBankService.addLessonWordsToUser(user.getUserId(), lessonId);
        return ResponseEntity.ok().build();
    }

    /**
     * Получаем все слова пользователя (Word Bank)
     */
    @GetMapping("/bank")
    public ResponseEntity<Page<WordResponse>> getWordBank(@AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "term") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<WordResponse> wordsPage = wordBankService.getAllWordsForUser(user.getUserId(), pageable);
        return ResponseEntity.ok(wordsPage);
    }

    // -------------------- Word Sets --------------------

    /**
     * Создаём новый Word Set
     */
    @PostMapping
    public ResponseEntity<WordSetResponse> createWordSet(@AuthenticationPrincipal User user,
            @RequestParam String name) {
        WordSetResponse set = wordSetService.createWordSet(user.getUserId(), name);
        return ResponseEntity.ok(set);
    }

    /**
     * Получаем все Word Sets пользователя
     */
    @GetMapping
    public ResponseEntity<List<WordSetResponse>> getWordSets(
            @AuthenticationPrincipal User user) {
        List<WordSetResponse> sets = wordSetService.getWordSetsForUser(user.getUserId());
        return ResponseEntity.ok(sets);
    }

    /**
     * Удаляем Word Set
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWordSet(@PathVariable UUID id) {
        wordSetService.deleteWordSet(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Переименовываем Word Set
     */
    @PatchMapping("/{id}/rename")
    public ResponseEntity<WordSetResponse> renameWordSet(@PathVariable UUID id,
            @RequestParam String name) {
        WordSetResponse set = wordSetService.renameWordSet(id, name);
        return ResponseEntity.ok(set);
    }

    /**
     * Добавляем слова в Word Set
     */
    @PostMapping("/set/{id}")
    public ResponseEntity<Void> addWordsToSet(@PathVariable UUID id,
            @RequestBody List<UUID> wordIds) {
        wordSetService.addWordsToSet(id, wordIds);
        return ResponseEntity.ok().build();
    }

    /**
     * Получаем слова Word Set в порядке позиции
     */
    @GetMapping("/set/{id}")
    public ResponseEntity<List<WordResponse>> getWordsInSet(@PathVariable UUID id) {
        return ResponseEntity.ok(wordSetService.getWordsInSet(id));
    }

    /**
     * Удаляем слово из Word Set
     */
    @DeleteMapping("/set/{id}")
    public ResponseEntity<Void> removeWordFromSet(@PathVariable UUID id,
            @RequestBody List<UUID> wordIds) {
        wordSetService.removeWordsFromSet(id, wordIds);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/set/total")
    public ResponseEntity<Long> getTotalSets(@AuthenticationPrincipal User user) {
        long total = wordSetService.getTotalSets(user.getUserId());
        return ResponseEntity.ok(total);
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<Void> shareWordSet(@PathVariable UUID id, @RequestParam UUID studentId) {
        wordSetService.shareWordSet(id, studentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/shared")
    public ResponseEntity<List<WordSetResponse>> getSharedWordSets(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(wordSetService.getSharedWordSets(user.getUserId()));
    }
}
