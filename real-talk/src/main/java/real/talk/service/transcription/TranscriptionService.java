package real.talk.service.transcription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import real.talk.model.dto.gladia.PreRecorderRequest;
import real.talk.model.dto.gladia.PreRecorderResponse;
import real.talk.model.dto.gladia.TranscriptionResultResponse;

import static real.talk.util.constants.Headers.GLADIA_KEY_HEADER;
import static real.talk.util.constants.URLs.GLADIA_PRE_RECORDER_URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptionService {

    @Value("${gladia.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public PreRecorderResponse transcribeAudio(String audioUrl) {
        PreRecorderRequest preRecorderRequest = PreRecorderRequest.builder().build();

        Mono<PreRecorderResponse> transcriptionIdMono = webClient.post()
                .uri(GLADIA_PRE_RECORDER_URL)
                .header(GLADIA_KEY_HEADER, apiKey)
                .bodyValue(preRecorderRequest)
                .retrieve()
                .bodyToMono(PreRecorderResponse.class)
                .map(response -> {
                    try {
                        return response;
                    } catch (Exception e) {
                        throw new RuntimeException("Ошибка парсинга ID транскрипции", e);
                    }
                });
        return transcriptionIdMono.block();
    }

    public TranscriptionResultResponse getTranscriptionResult(String transcriptionId) {
        Mono<TranscriptionResultResponse> resultMono = webClient.get()
                .uri(GLADIA_PRE_RECORDER_URL + "/{id}", transcriptionId)
                .header(GLADIA_KEY_HEADER, apiKey)
                .retrieve()
                .bodyToMono(TranscriptionResultResponse.class)
                .map(response -> {
                    try {
                        return response;
                    } catch (Exception e) {
                        throw new RuntimeException("Ошибка парсинга текста транскрипции", e);
                    }
                });

        return resultMono.block();
    }
}
