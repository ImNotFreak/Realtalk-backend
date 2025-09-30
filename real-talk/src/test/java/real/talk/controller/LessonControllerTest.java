package real.talk.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import real.talk.model.dto.lesson.LessonCreateRequest;
import real.talk.model.dto.lesson.LessonGeneratedByLlm;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.User;
import real.talk.service.lesson.LessonService;
import real.talk.service.user.UserService;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;




import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LessonsController.class)
class LessonsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private LessonService lessonService;

    @Test
    @DisplayName("POST /api/v1/lessons/create-lesson -> 200 и lessonIds[]")
    void createLesson_returnsLessonIds() throws Exception {
        Mockito.when(userService.saveUser(any(LessonCreateRequest.class)))
                .thenReturn(new User());

        UUID id = UUID.randomUUID();
        Lesson lessonMock = Mockito.mock(Lesson.class);
        Mockito.when(lessonMock.getId()).thenReturn(id);
        Mockito.when(lessonService.createLessons(any(User.class), any(LessonCreateRequest.class)))
                .thenReturn(List.of(lessonMock));

        String body = """
            {
              "name": "Unit Test Lesson",
              "order_number": "1",
              "youtube_links": ["https://www.youtube.com/watch?v=dQw4w9WgXcQ"],
              "email": "unit@test.example",
              "telegram": "@unit",
              "language": "English",
              "language_level": "B1",
              "grammar_topics": ["Present Perfect"]
            }
            """;

        mockMvc.perform(post("/api/v1/lessons/create-lesson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // ⚠️ тут camelCase, потому что в @WebMvcTest не подхватился SNAKE_CASE
                .andExpect(jsonPath("$.lessonIds", notNullValue()))
                .andExpect(jsonPath("$.lessonIds", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.lessonIds[0]", is(id.toString())));

        Mockito.verify(userService).saveUser(any(LessonCreateRequest.class));
        Mockito.verify(lessonService).createLessons(any(User.class), any(LessonCreateRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/lessons/public-lessons -> 200 и список объектов")
    void publicLessons_returns200AndList() throws Exception {
        LessonGeneratedByLlm dto = new LessonGeneratedByLlm();
        dto.setLessonTopic("Topic");

        Mockito.when(lessonService.getPublicReadyLessons())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/lessons/public-lessons"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].lessonTopic", is("Topic")));

        Mockito.verify(lessonService).getPublicReadyLessons();
    }

    @Test
    @DisplayName("POST /create-lesson -> 500 при исключении из LessonService")
    void createLesson_whenServiceThrows_returns500() throws Exception {
        when(userService.saveUser(any(LessonCreateRequest.class))).thenReturn(new User());
        when(lessonService.createLessons(any(User.class), any(LessonCreateRequest.class)))
                .thenThrow(new RuntimeException("boom"));

        String body = """
        {
          "name": "Err Case",
          "order_number": "1",
          "youtube_links": ["https://youtu.be/x"],
          "email": "err@test.example",
          "telegram": "@err",
          "language": "English",
          "language_level": "B1",
          "grammar_topics": ["Present Perfect"]
        }
        """;

        mockMvc.perform(post("/api/v1/lessons/create-lesson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Произошла ошибка"))); // подгони под фактическое сообщение, если иное
    }

    @Test
    @DisplayName("GET /public-lessons -> 200 и пустой массив")
    void publicLessons_empty_returnsEmptyArray() throws Exception {
        when(lessonService.getPublicReadyLessons()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/lessons/public-lessons"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST /create-lesson -> 400 при пустых обязательных полях")
    void createLesson_invalidRequest_returns400() throws Exception {
        // youtube_links отсутствует, name пустой — спровоцировать Bean Validation
        String badBody = """
        {
          "name": "",
          "order_number": "1",
          "email": "invalid@test.example",
          "telegram": "@inv",
          "language": "English",
          "language_level": "B1",
          "grammar_topics": []
        }
        """;

        mockMvc.perform(post("/api/v1/lessons/create-lesson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest());
        // Можно добавить точные проверки формата ошибок, когда определишь, как их сериализует ваш Handler:
        // .andExpect(jsonPath("$.errors", notNullValue()))
        // .andExpect(jsonPath("$.errors[*].field", hasItem("youtube_links")));
    }



}
