package real.talk.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import real.talk.model.dto.gladia.PreRecorderResponse;
import real.talk.model.dto.gladia.TranscriptionResultResponse;
import real.talk.model.dto.lesson.LessonRequest;
import real.talk.model.dto.lesson.LessonResponse;
import real.talk.model.entity.User;
import real.talk.service.transcription.GladiaService;
import real.talk.service.user.UserService;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/lessons")
@RequiredArgsConstructor
@Slf4j
class LessonsController {

    private final UserService userService;
    private final GladiaService gladiaService;

    @PostMapping("/generate-lesson")
    public Mono<ResponseEntity<PreRecorderResponse>> generateLesson(@RequestBody LessonRequest lessonRequest) {

        return Mono.fromCallable(() -> userService.saveUser(lessonRequest))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(user -> gladiaService.saveGladiaPreRecorderResponse(user, lessonRequest))
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Ошибка в цепочке generateLesson", e))
                .onErrorResume(e -> {
                    log.error("Возврат 500 Internal Server Error", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @GetMapping("/lesson")
    public Mono<ResponseEntity<TranscriptionResultResponse>> getReadyLesson(
            @RequestParam("userId") UUID userId,
            @RequestParam("fullUrl") String fullUrl) {

        return gladiaService.saveGladiaTranscriptionResultResponse(userId, fullUrl)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Ошибка в цепочке getReadyLesson", e))
                .onErrorResume(e -> {
                    log.error("Возврат 400 NOT FOUND", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
                });
    }
}
