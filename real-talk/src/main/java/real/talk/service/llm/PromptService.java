package real.talk.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.AssistantPromptTemplate;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import real.talk.model.dto.lesson.LessonGeneratedByLlm;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final ObjectMapper objectMapper;

    public Prompt createLessonPrompt(real.talk.model.entity.Lesson lesson, String transcription) throws IOException {
        String systemPromptString = Files.readString(
                new ClassPathResource("prompts/system.st").getFile().toPath()
        );

        String userPromptString = Files.readString(
                new ClassPathResource("prompts/user.st").getFile().toPath()
        );

        String assistantPromptString = Files.readString(
                new ClassPathResource("prompts/assistant.st").getFile().toPath()
        );

        String lessonJson = Files.readString(
                new ClassPathResource("prompts/lesson.json").getFile().toPath()
        );

        String grammarTopics = String.join(", ", lesson.getGrammarTopics());
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPromptString);
        String systemMessageText = systemPromptTemplate.render(
                Map.of("language", lesson.getLanguage(),
                        "languageLevel", lesson.getLanguageLevel(),
                        "grammarTopics", grammarTopics
                )
        );
        SystemMessage systemMessage = new SystemMessage(systemMessageText);

        PromptTemplate promptTemplate = new PromptTemplate(userPromptString);
        String userMessageText = promptTemplate.render(
                Map.of("language", lesson.getLanguage(),
                        "languageLevel", lesson.getLanguageLevel(),
                        "transcriptText", transcription,
                        "grammarTopics", grammarTopics));
        UserMessage userMessage = new UserMessage(userMessageText);

        AssistantPromptTemplate assistantPromptTemplate = new AssistantPromptTemplate(assistantPromptString);
        String lessonJsonText = assistantPromptTemplate.render(
                Map.of("lessonJson", lessonJson)
        );
        AssistantMessage assistantMessage = new AssistantMessage(lessonJsonText);

        return new Prompt(List.of(systemMessage, userMessage, assistantMessage));
    }

}
