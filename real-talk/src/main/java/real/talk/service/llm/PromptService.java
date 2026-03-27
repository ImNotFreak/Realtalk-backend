package real.talk.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import real.talk.model.dto.lesson.TranscriptAnalysis;
import real.talk.model.dto.lesson.WordBankResponse;
import real.talk.model.entity.Lesson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptService {

    private static final String DEFAULT_PRESET = "claudia";

    private final ObjectMapper objectMapper;

    public Prompt createAnalysisPrompt(Lesson lesson, String transcription) throws IOException {
        String coreSt = readResource("new_prompts/core.st");
        String transcriptSt = readResource("new_prompts/transcript.st");
        String cefrLevelsJson = readResource("new_prompts/CERF_levels.json");

        SystemMessage systemMessage = new SystemMessage(coreSt + "\n\n" + transcriptSt);

        Map<String, Object> inputs = new HashMap<>();
        String grammarConfig = buildGrammarConfig(lesson);
        inputs.put("lesson_inputs", Map.of(
                "transcript", transcription,
                "target_language", lesson.getLanguage(),
                "CERF_level", lesson.getLanguageLevel(),
                "grammar_point", grammarConfig
        ));
        inputs.put("cefr_level_config",
                objectMapper.readTree(cefrLevelsJson).path("levels").path(lesson.getLanguageLevel().toUpperCase()));
        inputs.put("grammar_config", grammarConfig);

        String userMessageText = objectMapper.writeValueAsString(inputs);
        UserMessage userMessage = new UserMessage(userMessageText);

        return new Prompt(List.of(systemMessage, userMessage));
    }

    public Prompt createWordBankPrompt(
            Lesson lesson,
            String transcription,
            TranscriptAnalysis transcriptAnalysis,
            List<Map<String, Object>> transcriptUtterances)
            throws IOException {
        String wordBankSt = readResource("new_prompts/word_bank.st");
        String cefrLevelsJson = readResource("new_prompts/CERF_levels.json");

        SystemMessage systemMessage = new SystemMessage(wordBankSt);

        Map<String, Object> inputs = new HashMap<>();
        Map<String, Object> lessonInputs = new HashMap<>();
        lessonInputs.put("transcript", transcription);
        lessonInputs.put("transcript_utterances", transcriptUtterances == null ? List.of() : transcriptUtterances);
        inputs.put("lesson_inputs", lessonInputs);
        inputs.put("transcript_analysis", transcriptAnalysis);
        inputs.put("target_language", lesson.getLanguage());
        inputs.put("cefr_level_config",
                objectMapper.readTree(cefrLevelsJson).path("levels").path(lesson.getLanguageLevel().toUpperCase()));

        String userMessageText = objectMapper.writeValueAsString(inputs);
        UserMessage userMessage = new UserMessage(userMessageText);

        return new Prompt(List.of(systemMessage, userMessage));
    }

    public Prompt createLessonGenerationPrompt(
            Lesson lesson,
            String transcription,
            TranscriptAnalysis transcriptAnalysis,
            WordBankResponse wordBank,
            JsonNode presetConfig) throws IOException {
        String lessonGenSt = readResource("new_prompts/lesson_generation.st");
        String cefrLevelsJson = readResource("new_prompts/CERF_levels.json");

        SystemMessage systemMessage = new SystemMessage(lessonGenSt);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("lesson_inputs", Map.of("transcript", transcription));
        inputs.put("transcript_analysis", transcriptAnalysis);
        inputs.put("word_bank", wordBank);
        inputs.put("target_language", lesson.getLanguage());
        inputs.put("cefr_level_config",
                objectMapper.readTree(cefrLevelsJson).path("levels").path(lesson.getLanguageLevel().toUpperCase()));
        inputs.put("grammar_topic_config", buildGrammarConfig(lesson));
        inputs.put("preset_config", presetConfig);

        String userMessageText = objectMapper.writeValueAsString(inputs);
        UserMessage userMessage = new UserMessage(userMessageText);

        return new Prompt(List.of(systemMessage, userMessage));
    }

    public Prompt createLessonRepairPrompt(String brokenLessonJson, String validationError, JsonNode presetConfig)
            throws IOException {
        String repairInstructions = """
                You are repairing a generated lesson JSON so it exactly matches the preset_config.

                Return only valid JSON with no markdown or commentary.
                Preserve valid content when possible.
                Every required key must be present.
                Include static-only blocks from preset_config.static exactly as provided when hydrated output requires them.
                Ensure each block follows output_schema structure from preset_config.
                Do not leave required answer fields null.
                """;

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("validation_error", validationError);
        inputs.put("broken_output", objectMapper.readTree(brokenLessonJson));
        inputs.put("preset_config", presetConfig);

        String userMessageText = objectMapper.writeValueAsString(inputs);
        return new Prompt(List.of(new SystemMessage(repairInstructions), new UserMessage(userMessageText)));
    }

    public JsonNode getPresetConfig(String presetName) throws IOException {
        String presetResourcePath = resolvePresetResourcePath(presetName);
        return objectMapper.readTree(readResource(presetResourcePath));
    }

    public String normalizePresetName(String presetName) {
        if (presetName == null || presetName.isBlank()) {
            return DEFAULT_PRESET;
        }
        return presetName.trim().toLowerCase();
    }

    private String resolvePresetResourcePath(String presetName) {
        String normalized = normalizePresetName(presetName);
        List<String> candidates = new ArrayList<>();
        candidates.add("new_prompts/" + normalized + ".json");

        if (DEFAULT_PRESET.equals(normalized)) {
            candidates.add("new_prompts/Claudia.json");
        }

        for (String candidate : candidates) {
            if (new ClassPathResource(candidate).exists()) {
                return candidate;
            }
        }

        throw new IllegalArgumentException("Preset not found: " + presetName);
    }

    private String readResource(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String buildGrammarConfig(Lesson lesson) {
        if (lesson.getGrammarTopics() == null || lesson.getGrammarTopics().isEmpty()) {
            return "";
        }
        List<String> topics = lesson.getGrammarTopics().stream()
                .filter(topic -> topic != null && !topic.isBlank())
                .map(String::trim)
                .toList();
        return String.join(", ", topics);
    }
}
