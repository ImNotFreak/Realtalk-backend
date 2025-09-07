package real.talk.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import real.talk.model.dto.lesson.LessonCreateRequest;
import real.talk.model.dto.lesson.LessonCreateResponse;
import real.talk.model.dto.lesson.LessonGeneratedByLlm;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.User;
import real.talk.service.lesson.LessonService;
import real.talk.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("api/v1/lessons")
@RequiredArgsConstructor
@Slf4j
class LessonsController {

    private final UserService userService;
    private final LessonService lessonService;

    @PostMapping("/create-lesson")
    public ResponseEntity<LessonCreateResponse> createLesson(@RequestBody LessonCreateRequest lessonRequest) {

        User user = userService.saveUser(lessonRequest);
        List<Lesson> lessons = lessonService.createLessons(user, lessonRequest);
        LessonCreateResponse createLessonResponse = LessonCreateResponse.builder()
                .lessonIds(lessons.stream().map(Lesson::getId).toList())
                .build();
        return ResponseEntity.ok(createLessonResponse);
    }

    @GetMapping("/public-lessons")
    public ResponseEntity<List<LessonGeneratedByLlm>> getPublicLessons() {
        return ResponseEntity.ok(lessonService.getPublicReadyLessons());
    }

}
