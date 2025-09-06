package real.talk.service.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import real.talk.model.entity.LlmData;
import real.talk.model.entity.enums.DataStatus;
import real.talk.repository.llm.LlmDataRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LlmDataService {

    private final LlmDataRepository llmDataRepository;

    public Optional<LlmData> getLlmDataByLessonId(UUID lessonId){
        return llmDataRepository.findByLessonId(lessonId);
    }

    public LlmData save(LlmData llmData){
        return llmDataRepository.save(llmData);
    }

    public boolean existsByLessonIdAndStatusDone(UUID lessonId){
        return llmDataRepository.existsByLessonIdAndStatus(lessonId, DataStatus.DONE);
    }
}
