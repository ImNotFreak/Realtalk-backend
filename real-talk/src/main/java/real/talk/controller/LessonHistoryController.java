package real.talk.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import real.talk.model.dto.history.LessonHistoryResponse;
import real.talk.model.entity.enums.LessonAccess;
import real.talk.model.entity.enums.LessonStatus;
import real.talk.service.history.LessonHistoryService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/history")
@RequiredArgsConstructor
@Slf4j
public class LessonHistoryController {

    private final LessonHistoryService historyService;

    @PostMapping("/lesson/{lessonId}/open")
    public ResponseEntity<Void> trackOpen(@PathVariable UUID lessonId,
                                          @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        historyService.trackLessonOpen(userId, lessonId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    public Map<String, List<LessonHistoryResponse>> getHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "READY") LessonStatus status,
            @RequestParam(defaultValue = "PUBLIC") LessonAccess access,
            Pageable pageable) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        return historyService.getHistory(userId, status, access, pageable);
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotalHistory(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        long total = historyService.getTotalHistory(userId);
        return ResponseEntity.ok(total);
    }
}
