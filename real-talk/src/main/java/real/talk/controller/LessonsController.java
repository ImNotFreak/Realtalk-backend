package real.talk.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import real.talk.model.dto.lesson.LessonRequest;
import real.talk.model.dto.lesson.LessonResponse;
import real.talk.service.user.UserService;

@RestController
@RequestMapping("api/v1/lessons")
@RequiredArgsConstructor
class LessonsController {

    private final UserService userService;

    @PostMapping("/generate-lesson")
    ResponseEntity<LessonResponse> generateLesson(@RequestBody LessonRequest lessonRequest) {

        userService.saveUser(lessonRequest);
        return ResponseEntity.ok(LessonResponse.builder().build());
    }

}
