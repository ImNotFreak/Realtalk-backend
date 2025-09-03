package real.talk.service.transcription;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import real.talk.model.dto.gladia.PreRecorderRequest;
import real.talk.model.dto.gladia.PreRecorderResponse;
import real.talk.model.dto.gladia.TranscriptionResultResponse;

import java.io.IOException;
import java.util.UUID;

import static real.talk.util.constants.Headers.GLADIA_KEY_HEADER;
import static real.talk.util.constants.URLs.GLADIA_PRE_RECORDER_URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptionService {

    @Value("${gladia.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public Mono<PreRecorderResponse> transcribeAudio(String audioUrl) {
        PreRecorderRequest preRecorderRequest = PreRecorderRequest.builder()
                .audioUrl(audioUrl)
                .build();

//        return webClient.post()
//                .uri(GLADIA_PRE_RECORDER_URL)
//                .header(GLADIA_KEY_HEADER, apiKey)
//                .bodyValue(preRecorderRequest)
//                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
//                        .flatMap(body -> {
//                            log.error("Gladia вернул 4xx: {}", body);
//                            return Mono.error(new RuntimeException("Client Error: " + body));
//                        }))
//                .onStatus(HttpStatusCode::is5xxServerError, response ->
//                        Mono.error(new RuntimeException("Server Error: " + response.statusCode())))
//                .bodyToMono(PreRecorderResponse.class);
                    return null;
    }

    public Mono<TranscriptionResultResponse> getTranscriptionResult(UUID transcriptionId) {
        return webClient.get()
                .uri(GLADIA_PRE_RECORDER_URL + "/" + transcriptionId)
                .header(GLADIA_KEY_HEADER, apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new RuntimeException("Client Error: " + response.statusCode())))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new RuntimeException("Server Error: " + response.statusCode())))
                .bodyToMono(TranscriptionResultResponse.class);
    }

}
