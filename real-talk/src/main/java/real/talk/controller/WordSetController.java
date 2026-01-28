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
import real.talk.model.entity.words.Word;
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
    @PostMapping("/add")
    public ResponseEntity<Void> addLessonWords(@AuthenticationPrincipal Jwt jwt,
                                               @RequestParam UUID lessonId) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        wordBankService.addLessonWordsToUser(userId, lessonId);
        return ResponseEntity.ok().build();
    }

    /**
     * Получаем все слова пользователя (Word Bank)
     */
    @GetMapping("/bank")
    public ResponseEntity<Page<WordResponse>> getWordBank(@AuthenticationPrincipal Jwt jwt,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "50") int size,
                                                          @RequestParam(defaultValue = "term") String sortBy,
                                                          @RequestParam(defaultValue = "asc") String sortDir) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<WordResponse> wordsPage = wordBankService.getAllWordsForUser(userId, pageable);
        return ResponseEntity.ok(wordsPage);
    }

    // -------------------- Word Sets --------------------

    /**
     * Создаём новый Word Set
     */
    @PostMapping
    public ResponseEntity<WordSetResponse> createWordSet(@AuthenticationPrincipal Jwt jwt,
                                                         @RequestParam String name) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        WordSetResponse set = wordSetService.createWordSet(userId, name);
        return ResponseEntity.ok(set);
    }

    /**
     * Получаем все Word Sets пользователя
     */
    @GetMapping
    public ResponseEntity<List<WordSetResponse>> getWordSets(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        List<WordSetResponse> sets = wordSetService.getWordSetsForUser(userId);
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
    @PostMapping("/{id}/words")
    public ResponseEntity<Void> addWordsToSet(@PathVariable UUID id,
                                              @RequestBody List<UUID> wordIds) {
        wordSetService.addWordsToSet(id, wordIds);
        return ResponseEntity.ok().build();
    }

    /**
     * Получаем слова Word Set в порядке позиции
     */
    @GetMapping("/{id}/words")
    public ResponseEntity<List<WordResponse>> getWordsInSet(@PathVariable UUID id) {
        return ResponseEntity.ok(wordSetService.getWordsInSet(id));
    }

    /**
     * Удаляем слово из Word Set
     */
    @DeleteMapping("/{id}/words/{wordId}")
    public ResponseEntity<Void> removeWordFromSet(@PathVariable UUID id,
                                                  @PathVariable UUID wordId) {
        wordSetService.removeWordFromSet(id, wordId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotalSets(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        long total = wordSetService.getTotalSets(userId);
        return ResponseEntity.ok(total);
    }
}
