package real.talk.service.transcription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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
                                                                                   UUID transcriptionId) {
        return Mono.fromCallable(() -> gladiaDataRepository.findGladiaDataByGladiaRequestId(transcriptionId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalData -> {
                    if (optionalData.isPresent() && optionalData.get().getData() != null) {
                        // ✅ Запись уже есть в БД → возвращаем из базы
                        log.info("Транскрипция для userId={} уже есть в БД, API не вызываем", userId);
                        return Mono.just(optionalData.get().getData());
                    } else {
                        // ❌ В базе нет → идём в API
                        log.info("Транскрипции для userId={} нет в БД, вызываем API", userId);
                        return transcriptionService.getTranscriptionResult(transcriptionId)
                                .flatMap(transcriptionResult ->
                                        Mono.fromCallable(() -> {
                                                    GladiaData gladiaData = optionalData.orElseGet(() -> new GladiaData());
                                                    gladiaData.setData(transcriptionResult);
                                                    return gladiaDataRepository.save(gladiaData);
                                                })
                                                .subscribeOn(Schedulers.boundedElastic())
                                                .thenReturn(transcriptionResult)
                                );
                    }
                });
    }

}
