package real.talk.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import real.talk.model.dto.like.LessonLikeResponse;
import real.talk.service.lesson.LessonService;
import real.talk.service.like.LessonLikeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/likes")
class LessonLikeController {

    private final LessonLikeService lessonLikeService;

    @PostMapping("/{lessonId}/like")
    public ResponseEntity<Void> likeLesson(@PathVariable UUID lessonId,
                                           @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        lessonLikeService.likeLesson(userId, lessonId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{lessonId}/like")
    public ResponseEntity<Void> unlikeLesson(@PathVariable UUID lessonId,
                                             @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        lessonLikeService.unlikeLesson(userId, lessonId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ids")
    public ResponseEntity<List<UUID>> getLikedLessonIds(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        return ResponseEntity.ok(lessonLikeService.getLikedLessonIds(userId));
    }

    @GetMapping("")
    public ResponseEntity<List<LessonLikeResponse>> getLikedLessons(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        return ResponseEntity.ok(lessonLikeService.getLikedLessons(userId));
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotalLikes(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        long total = lessonLikeService.getTotalLikes(userId);
        return ResponseEntity.ok(total);
    }
}
