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
            @AuthenticationPrincipal real.talk.model.entity.User user) {
        lessonLikeService.likeLesson(user.getUserId(), lessonId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{lessonId}/like")
    public ResponseEntity<Void> unlikeLesson(@PathVariable UUID lessonId,
            @AuthenticationPrincipal real.talk.model.entity.User user) {
        lessonLikeService.unlikeLesson(user.getUserId(), lessonId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ids")
    public ResponseEntity<List<UUID>> getLikedLessonIds(@AuthenticationPrincipal real.talk.model.entity.User user) {
        return ResponseEntity.ok(lessonLikeService.getLikedLessonIds(user.getUserId()));
    }

    @GetMapping("")
    public ResponseEntity<List<LessonLikeResponse>> getLikedLessons(
            @AuthenticationPrincipal real.talk.model.entity.User user) {
        return ResponseEntity.ok(lessonLikeService.getLikedLessons(user.getUserId()));
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotalLikes(@AuthenticationPrincipal real.talk.model.entity.User user) {
        long total = lessonLikeService.getTotalLikes(user.getUserId());
        return ResponseEntity.ok(total);
    }
}
