package real.talk.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.AssistantPromptTemplate;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import real.talk.model.entity.Lesson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptService {

    public Prompt createLessonPrompt(Lesson lesson, String transcription) throws IOException {
        String systemPromptString = readResource("prompts/system.st");
        String userPromptString = readResource("prompts/user.st");
        String assistantPromptString = readResource("prompts/assistant.st");
        String lessonJson = readResource("prompts/lesson.json");

        String grammar_topics = String.join(", ", lesson.getGrammarTopics());
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPromptString);
        String systemMessageText = systemPromptTemplate.render(
                Map.of("language", lesson.getLanguage(),
                        "language_level", lesson.getLanguageLevel(),
                        "grammar_topics", grammar_topics
                )
        );
        SystemMessage systemMessage = new SystemMessage(systemMessageText);

        PromptTemplate promptTemplate = new PromptTemplate(userPromptString);
        String userMessageText = promptTemplate.render(
                Map.of("language", lesson.getLanguage(),
                        "language_level", lesson.getLanguageLevel(),
                        "transcript_text", transcription,
                        "grammar_topics", grammar_topics));
        UserMessage userMessage = new UserMessage(userMessageText);

        AssistantPromptTemplate assistantPromptTemplate = new AssistantPromptTemplate(assistantPromptString);
        String lessonJsonText = assistantPromptTemplate.render(
                Map.of("lessonJson", lessonJson)
        );
        AssistantMessage assistantMessage = new AssistantMessage(lessonJsonText);

        return new Prompt(List.of(systemMessage, userMessage, assistantMessage));
    }

    private String readResource(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
