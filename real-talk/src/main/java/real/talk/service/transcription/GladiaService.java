package real.talk.service.transcription;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import real.talk.model.dto.gladia.PreRecorderResponse;
import real.talk.model.dto.gladia.TranscriptionResultResponse;
import real.talk.model.dto.lesson.LessonRequest;
import real.talk.model.entity.GladiaData;
import real.talk.model.entity.User;
import real.talk.repository.gladia.GladiaDataRepository;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GladiaService {

    private final TranscriptionService transcriptionService;
    private final GladiaDataRepository gladiaDataRepository;


    @Transactional
    public Mono<PreRecorderResponse> saveGladiaPreRecorderResponse(User user,
                                                             LessonRequest lessonRequest) {

        return transcriptionService.transcribeAudio(lessonRequest.getYoutubeLink())
                .flatMap(preRecorderResponse -> {
                    GladiaData gladiaData = new GladiaData();
                    gladiaData.setUser(user);
                    gladiaData.setGladiaRequestId(preRecorderResponse.getId());
                    gladiaData.setGladiaFullUrl(preRecorderResponse.getResultUrl());
                    // JPA репозиторий блокирующий, оборачиваем в boundedElastic
                    return Mono.fromCallable(() -> gladiaDataRepository.save(gladiaData))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(saved -> preRecorderResponse); // возвращаем оригинальный ответ
                });
    }

    @Transactional
    public Mono<TranscriptionResultResponse> saveGladiaTranscriptionResultResponse(UUID userId,
                                                                                   String transcriptionUrl) {
        return transcriptionService.getTranscriptionResult(transcriptionUrl)
                .flatMap(transcriptionResult ->
                        Mono.fromCallable(() -> gladiaDataRepository.findGladiaDataByUserUserId(userId)
                                        .orElseThrow(() -> new RuntimeException("GladiaData not found for user: " + userId)))
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMap(gladiaData -> {
                                    gladiaData.setData(transcriptionResult);
                                    // Сохраняем блокирующий репозиторий в отдельном потоке
                                    return Mono.fromCallable(() -> gladiaDataRepository.save(gladiaData))
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .thenReturn(transcriptionResult);
                                })
                );
    }

}
