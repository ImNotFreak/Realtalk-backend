package real.talk.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import real.talk.model.dto.lesson.LessonRequest;
import real.talk.model.dto.lesson.LessonResponse;
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
    public ResponseEntity<LessonResponse> createLesson(@RequestBody LessonRequest lessonRequest) {

        User user = userService.saveUser(lessonRequest);
        List<Lesson> lessons = lessonService.createLessons(user, lessonRequest);
        LessonResponse createLessonResponse = LessonResponse.builder()
                .lessonIds(lessons.stream().map(Lesson::getId).toList())
                .build();
        return ResponseEntity.ok(createLessonResponse);
    }

}
