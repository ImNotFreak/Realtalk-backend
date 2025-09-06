package real.talk.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import real.talk.model.dto.llm.LlmResponse;
import real.talk.model.entity.Lesson;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final ObjectMapper objectMapper;

    public Prompt createLessonPrompt(Lesson lesson, String transcription) throws IOException {
        String lessonPromptString = Files.readString(
                new ClassPathResource("prompts/lesson.st").getFile().toPath()
        );

        String transcriptionPromptString = Files.readString(
                new ClassPathResource("prompts/transcription.st").getFile().toPath()
        );

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getLlmRepsonseExample());
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(lessonPromptString);
        Message systemMessage = systemPromptTemplate.createMessage(
                Map.of("languageLevel", lesson.getLanguageLevel(),
                        "grammarTopics", String.join(", ", lesson.getGrammarTopics()),
                        "lessonJson", json
                )
        );

        PromptTemplate promptTemplate = new PromptTemplate(transcriptionPromptString);
        Message userMessage = promptTemplate.createMessage(
                Map.of("transcription", transcription));

        return new Prompt(List.of(systemMessage, userMessage));
    }

    private LlmResponse getLlmRepsonseExample(){
        LlmResponse response = new LlmResponse();

        LlmResponse.GlossaryItem item = new LlmResponse.GlossaryItem(
                "expression",
                "Цитата из подкаста",
                "Перевод",
                "Объяснение",
                "Другой пример"
        );
        response.setGlossary(List.of(item));

        LlmResponse.Exercise lexical = new LlmResponse.Exercise(
                "Вставь пропущенное слово",
                List.of("пример1", "пример2")
        );
        response.setLexicalExercises(List.of(lexical));

        LlmResponse.Exercise grammar = new LlmResponse.Exercise(
                "Present Perfect",
                List.of("пример1", "пример2")
        );
        response.setGrammarExercises(List.of(grammar));

        response.setQuizlet(List.of("термин, перевод", "термин2, перевод2"));

        LlmResponse.Answers answers = new LlmResponse.Answers(
                List.of("ответ1", "ответ2"),
                List.of("ответ1", "ответ2")
        );
        response.setAnswers(answers);

        return response;
    }
}
