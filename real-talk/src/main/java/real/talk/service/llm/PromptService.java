package real.talk.service.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
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
        String lessonPromptString = Files.readString(
                new ClassPathResource("prompts/lesson.st").getFile().toPath()
        );

        String transcriptionPromptString = Files.readString(
                new ClassPathResource("prompts/transcription.st").getFile().toPath()
        );

        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(getLlmRepsonseExample());
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(lessonPromptString);
        Message systemMessage = systemPromptTemplate.createMessage(
                Map.of("language", lesson.getLanguage(),
                        "languageLevel", lesson.getLanguageLevel(),
                        "grammarTopics", String.join(", ", lesson.getGrammarTopics()),
                        "lessonJson", json
                )
        );

        PromptTemplate promptTemplate = new PromptTemplate(transcriptionPromptString);
        Message userMessage = promptTemplate.createMessage(
                Map.of("transcription", transcription));

        return new Prompt(List.of(systemMessage, userMessage));
    }

    private LessonGeneratedByLlm getLlmRepsonseExample(){
        LessonGeneratedByLlm response = new LessonGeneratedByLlm();
        response.setLessonTopic("Общая тема урока");
        LessonGeneratedByLlm.GlossaryItem item = new LessonGeneratedByLlm.GlossaryItem(
                "expression",
                "Цитата из подкаста",
                "Перевод",
                "Объяснение",
                "Другой пример",
                null
        );
        response.setGlossary(List.of(item));

        LessonGeneratedByLlm.Exercise fillTheBlank = new LessonGeneratedByLlm.Exercise(
                "Вставь пропущенное слово",
                "Используйте выражения:",
                List.of("пример1", "пример2")
        );

        LessonGeneratedByLlm.Exercise truOrFalse = new LessonGeneratedByLlm.Exercise(
                "Игра «Ложь или правда?»",
                "Определи, правда или ложь. Если ложь, исправь предложение.",
                List.of("пример1", "пример2")
        );
        LessonGeneratedByLlm.Exercise listening = new LessonGeneratedByLlm.Exercise(
                "Послушай и отметь слова из списка:",
                null,
                List.of("пример1", "пример2")
        );
        response.setLexicalExercises(List.of(fillTheBlank, truOrFalse, listening));

        LessonGeneratedByLlm.Exercise grammar1 = new LessonGeneratedByLlm.Exercise(
                "Grammar exercise",
                null,
                List.of("пример1", "пример2")
        );
        LessonGeneratedByLlm.Exercise grammar2 = new LessonGeneratedByLlm.Exercise(
                "Grammar exercise",
                null,
                List.of("пример1", "пример2")
        );
        LessonGeneratedByLlm.Exercise grammar3 = new LessonGeneratedByLlm.Exercise(
                "Grammar exercise",
                null,
                List.of("пример1", "пример2")
        );
        response.setGrammarExercises(List.of(grammar1, grammar2, grammar3));

        response.setQuizlet(List.of("термин, перевод", "термин2, перевод2"));

        LessonGeneratedByLlm.Answers fillTheBlankAnswers = new LessonGeneratedByLlm.Answers(
                "fillTheBlankAnswers",
                List.of("пример1", "пример2")
        );

        LessonGeneratedByLlm.Answers truOrFalseAnswers = new LessonGeneratedByLlm.Answers(
                "truOrFalseAnswers",
                List.of("пример1", "пример2")
        );

        LessonGeneratedByLlm.Answers grammar1Answers = new LessonGeneratedByLlm.Answers(
                "grammar1Answers",
                List.of("пример1", "пример2")
        );

        LessonGeneratedByLlm.Answers grammar2Answers = new LessonGeneratedByLlm.Answers(
                "grammar2Answers",
                List.of("пример1", "пример2")
        );

        LessonGeneratedByLlm.Answers grammar3Answers = new LessonGeneratedByLlm.Answers(
                "grammar3Answers",
                List.of("пример1", "пример2")
        );


        response.setLexicalAnswers(List.of(fillTheBlankAnswers, truOrFalseAnswers));
        response.setGrammarAnswers(List.of(grammar1Answers, grammar2Answers, grammar3Answers));

        return response;
    }
}
