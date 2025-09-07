package real.talk.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import real.talk.model.dto.lesson.LessonGeneratedByLlm;
import real.talk.model.entity.GladiaData;

@Service
@RequiredArgsConstructor
@Slf4j
public class GptLessonService {

    private final PromptService promptService;
    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    public LessonGeneratedByLlm createLesson(real.talk.model.entity.Lesson lesson, GladiaData gladiaData) {
        try {
            // 1️⃣ Первый запрос: создаём контекст на основе Lesson
            Prompt lessonPrompt = promptService.createLessonPrompt(lesson, gladiaData.getData().getResult().getTranscription().getFullTranscript());
            ChatResponse response = chatModel.call(lessonPrompt); // ответ первого запроса не нужен

            // 3️⃣ Получаем JSON строку и парсим в LessonJsonResponse
            String gptReply = response.getResult().getOutput().getText();
            log.info("Ответ GPT на расшифровку: {}", gptReply);
            return objectMapper.readValue(gptReply, LessonGeneratedByLlm.class);

        } catch (Exception e) {
            log.error("Ошибка генерации урока {}", lesson.getId(), e);
            throw new RuntimeException(e);
        }
    }
}
