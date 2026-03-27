package real.talk.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import real.talk.model.dto.lesson.GeneratedPreset;
import real.talk.model.dto.lesson.TranscriptAnalysis;
import real.talk.model.dto.lesson.WordBankResponse;
import real.talk.model.entity.GladiaData;
import real.talk.model.entity.Lesson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GptLessonService {

    private final PromptService promptService;
    private final OpenAiChatModel chatModel;
    private final ObjectMapper objectMapper;

    public GeneratedPreset createLesson(Lesson lesson, GladiaData gladiaData) {
        try {
            String fullTranscript = buildTranscriptForLesson(lesson, gladiaData);
            List<real.talk.model.dto.gladia.TranscriptionResultResponse.Utterance> lessonUtterances =
                    buildUtterancesForLesson(lesson, gladiaData);
            List<Map<String, Object>> promptUtterances = lessonUtterances.stream()
                    .filter(Objects::nonNull)
                    .map(u -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("text", Objects.toString(u.getText(), ""));
                        item.put("start", u.getStart());
                        item.put("end", u.getEnd());
                        return item;
                    })
                    .toList();
            String presetName = promptService.normalizePresetName(lesson.getPreset());
            JsonNode presetConfig = promptService.getPresetConfig(presetName);
            List<Message> conversation = new ArrayList<>();

            log.info("Step 1: Analyzing transcript for lesson {}", lesson.getId());
            Prompt analysisPrompt = promptService.createAnalysisPrompt(lesson, fullTranscript);
            conversation.addAll(analysisPrompt.getInstructions());
            ChatResponse analysisResponse = chatModel.call(analysisPrompt);
            String analysisJson = analysisResponse.getResult().getOutput().getText();
            conversation.add(new AssistantMessage(analysisJson));
            log.info("Step 1 Response (Transcript Analysis):\n{}", analysisJson);
            TranscriptAnalysis transcriptAnalysis = objectMapper.readValue(analysisJson, TranscriptAnalysis.class);

            log.info("Step 2: Curating vocabulary for lesson {}", lesson.getId());
            Prompt wordBankPrompt = promptService.createWordBankPrompt(
                    lesson,
                    fullTranscript,
                    transcriptAnalysis,
                    promptUtterances);
            conversation.addAll(wordBankPrompt.getInstructions());
            ChatResponse wordBankResponse = chatModel.call(new Prompt(conversation));
            String wordBankJson = wordBankResponse.getResult().getOutput().getText();
            conversation.add(new AssistantMessage(wordBankJson));
            log.info("Step 2 Response (Word Bank):\n{}", wordBankJson);
            WordBankResponse wordBank = objectMapper.readValue(wordBankJson, WordBankResponse.class);
            normalizeWordBankTimestamps(wordBank);

            log.info("Step 3: Generating final lesson for lesson {} with preset {}", lesson.getId(), presetName);
            Prompt lessonGenerationPrompt = promptService.createLessonGenerationPrompt(
                    lesson,
                    fullTranscript,
                    transcriptAnalysis,
                    wordBank,
                    presetConfig);
            conversation.addAll(lessonGenerationPrompt.getInstructions());
            ChatResponse lessonResponse = chatModel.call(new Prompt(conversation));
            String finalLessonJson = sanitizeJson(lessonResponse.getResult().getOutput().getText());
            conversation.add(new AssistantMessage(finalLessonJson));
            log.info("Step 3 Response (Lesson Generation):\n{}", finalLessonJson);

            JsonNode presetData = mapLessonWithRepair(lesson, presetConfig, conversation, finalLessonJson);
            log.info("Mapped preset data for lesson {} preset {}", lesson.getId(), presetName);

            return GeneratedPreset.builder()
                    .tag(transcriptAnalysis.getLexical_field())
                    .wordBankResponse(wordBank)
                    .preset(presetData)
                    .build();
        } catch (Exception e) {
            log.error("Error generating lesson {}", lesson.getId(), e);
            throw new RuntimeException(e);
        }
    }

    private String buildTranscriptForLesson(Lesson lesson, GladiaData gladiaData) {
        if (gladiaData == null || gladiaData.getData() == null
                || gladiaData.getData().getResult() == null
                || gladiaData.getData().getResult().getTranscription() == null) {
            return "";
        }

        String fullTranscript = gladiaData.getData().getResult().getTranscription().getFullTranscript();
        Double segmentStartMin = lesson.getSegmentStartMin();
        Double segmentEndMin = lesson.getSegmentEndMin();

        if (segmentStartMin == null || segmentEndMin == null) {
            return Objects.toString(fullTranscript, "");
        }
        double segmentStartSec = segmentStartMin * 60.0;
        double segmentEndSec = segmentEndMin * 60.0;

        List<real.talk.model.dto.gladia.TranscriptionResultResponse.Utterance> utterances =
                buildUtterancesForLesson(lesson, gladiaData);
        if (utterances == null || utterances.isEmpty()) {
            return Objects.toString(fullTranscript, "");
        }

        String segmentTranscript = utterances.stream()
                .filter(Objects::nonNull)
                .filter(u -> u.getEnd() > segmentStartSec && u.getStart() < segmentEndSec)
                .map(real.talk.model.dto.gladia.TranscriptionResultResponse.Utterance::getText)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));

        if (!segmentTranscript.isBlank()) {
            return segmentTranscript;
        }

        return Objects.toString(fullTranscript, "");
    }

    private List<real.talk.model.dto.gladia.TranscriptionResultResponse.Utterance> buildUtterancesForLesson(
            Lesson lesson,
            GladiaData gladiaData) {
        if (gladiaData == null || gladiaData.getData() == null
                || gladiaData.getData().getResult() == null
                || gladiaData.getData().getResult().getTranscription() == null
                || gladiaData.getData().getResult().getTranscription().getUtterances() == null) {
            return List.of();
        }

        List<real.talk.model.dto.gladia.TranscriptionResultResponse.Utterance> utterances = gladiaData.getData()
                .getResult().getTranscription().getUtterances();

        Double segmentStartMin = lesson.getSegmentStartMin();
        Double segmentEndMin = lesson.getSegmentEndMin();
        if (segmentStartMin == null || segmentEndMin == null) {
            return utterances;
        }

        double segmentStartSec = segmentStartMin * 60.0;
        double segmentEndSec = segmentEndMin * 60.0;

        return utterances.stream()
                .filter(Objects::nonNull)
                .filter(u -> u.getEnd() > segmentStartSec && u.getStart() < segmentEndSec)
                .toList();
    }

    private JsonNode mapLessonWithRepair(Lesson lesson, JsonNode presetConfig, List<Message> conversation, String lessonJson)
            throws Exception {
        JsonNode normalizedLessonNode = normalizeLessonNode(extractLessonNode(lessonJson), presetConfig);

        try {
            return mapAndValidate(normalizedLessonNode, presetConfig);
        } catch (Exception e) {
            log.warn("Lesson {} failed validation, attempting repair: {}", lesson.getId(), e.getMessage());
            Prompt repairPrompt = promptService.createLessonRepairPrompt(
                    normalizedLessonNode.toString(),
                    e.getMessage(),
                    presetConfig);
            conversation.addAll(repairPrompt.getInstructions());
            ChatResponse repairResponse = chatModel.call(new Prompt(conversation));
            String repairedJson = sanitizeJson(repairResponse.getResult().getOutput().getText());
            log.info("Repair response for lesson {}:\n{}", lesson.getId(), repairedJson);
            JsonNode repairedLessonNode = normalizeLessonNode(extractLessonNode(repairedJson), presetConfig);
            return mapAndValidate(repairedLessonNode, presetConfig);
        }
    }

    private JsonNode mapAndValidate(JsonNode lessonNode, JsonNode presetConfig) {
        log.info("Final lessonNode before validation: {}", lessonNode.toPrettyString());
        validateGeneratedLesson(lessonNode, presetConfig);
        return lessonNode;
    }

    private JsonNode extractLessonNode(String lessonJson) throws Exception {
        JsonNode rootNode = objectMapper.readTree(lessonJson);
        if (rootNode.has("dynamic_lesson")) {
            return rootNode.get("dynamic_lesson");
        }
        if (rootNode.has("dynamic")) {
            return rootNode.get("dynamic");
        }
        return rootNode;
    }

    private JsonNode normalizeLessonNode(JsonNode lessonNode, JsonNode presetConfig) {
        JsonNode copy = lessonNode.deepCopy();

        if (!copy.isObject()) {
            return copy;
        }

        ObjectNode objNode = (ObjectNode) copy;
        List<String> fieldNames = new ArrayList<>();
        objNode.fieldNames().forEachRemaining(fieldNames::add);
        for (String fieldName : fieldNames) {
            JsonNode blockNode = objNode.get(fieldName);
            if (blockNode != null && blockNode.isObject() && blockNode.has("content")) {
                objNode.set(fieldName, blockNode.get("content"));
            }
        }

        JsonNode blocks = presetConfig.path("output_contract").path("blocks");
        if (blocks.isArray()) {
            for (JsonNode block : blocks) {
                if (block.path("is_static_block").asBoolean(false)) {
                    String blockId = block.path("id").asText();
                    if (!objNode.has(blockId) && block.has("static")) {
                        objNode.set(blockId, block.get("static"));
                    }
                }
            }
        }

        return copy;
    }

    private void validateGeneratedLesson(JsonNode lessonNode, JsonNode presetConfig) {
        if (!lessonNode.isObject()) {
            throw new IllegalArgumentException("Generated lesson must be a JSON object");
        }

        JsonNode outputContract = presetConfig.path("output_contract");
        String generationMode = outputContract.path("generation_mode").asText("hydrated_lesson");
        JsonNode blocks = outputContract.path("blocks");
        if (!blocks.isArray()) {
            throw new IllegalArgumentException("Preset config does not define output_contract.blocks");
        }

        for (JsonNode block : blocks) {
            String blockId = block.path("id").asText();
            if (blockId.isBlank()) {
                continue;
            }
            boolean isStatic = block.path("is_static_block").asBoolean(false);
            if ("dynamic_only".equals(generationMode) && isStatic) {
                continue;
            }
            JsonNode value = lessonNode.get(blockId);
            if (value == null || value.isNull()) {
                throw new IllegalArgumentException("Missing block in generated lesson: " + blockId);
            }
        }
    }

    private String sanitizeJson(String rawResponse) {
        String json = rawResponse.trim();
        if (json.startsWith("```json")) {
            json = json.substring(7);
        } else if (json.startsWith("```")) {
            json = json.substring(3);
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }
        return json.trim();
    }

    private void normalizeWordBankTimestamps(WordBankResponse wordBank) {
        if (wordBank == null || wordBank.getItems() == null) {
            return;
        }

        for (WordBankResponse.GlossaryItem item : wordBank.getItems()) {
            if (item == null) {
                continue;
            }
            Long totalSeconds = parseTimestampToSeconds(item.getTimestamp());
            item.setTimestamp(totalSeconds == null ? null : formatMmSs(totalSeconds));
        }
    }

    private Long parseTimestampToSeconds(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().replace(',', '.');
        try {
            if (normalized.contains(":")) {
                String[] parts = normalized.split(":");
                if (parts.length == 2) {
                    long minutes = (long) Math.floor(Double.parseDouble(parts[0].trim()));
                    long seconds = (long) Math.floor(Double.parseDouble(parts[1].trim()));
                    long total = minutes * 60 + seconds;
                    return total >= 0 ? total : null;
                }
                if (parts.length == 3) {
                    long hours = (long) Math.floor(Double.parseDouble(parts[0].trim()));
                    long minutes = (long) Math.floor(Double.parseDouble(parts[1].trim()));
                    long seconds = (long) Math.floor(Double.parseDouble(parts[2].trim()));
                    long total = hours * 3600 + minutes * 60 + seconds;
                    return total >= 0 ? total : null;
                }
                return null;
            }

            long total = (long) Math.floor(Double.parseDouble(normalized));
            return total >= 0 ? total : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatMmSs(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
