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
import real.talk.model.dto.lesson.LessonFilter;


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
    public ResponseEntity<List<LessonGeneratedByLlm>> getPublicLessons(
            @RequestParam(required = false) String language,
            @RequestParam(name = "language_level", required = false) String languageLevel,
            @RequestParam(name = "lesson_topic", required = false) String lessonTopic,
            @RequestParam(name = "grammar_contains", required = false) String grammarContains,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
                var filter = LessonFilter.builder()
               .language(blankToNull(language))
               .languageLevel(blankToNull(languageLevel))
               .lessonTopic(blankToNull(lessonTopic))
               .grammarContains(blankToNull(grammarContains))
               .page(page)
               .size(size)
               .sort(blankToNull(sort))
               .build();
       var resultPage = lessonService.getPublicReadyLessons(filter); // Page<LessonGeneratedByLlm>
       return ResponseEntity.ok(resultPage.getContent());            // отдаем List, без метаданных
       }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

}
