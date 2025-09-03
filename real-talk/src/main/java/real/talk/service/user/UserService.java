package real.talk.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import real.talk.model.dto.lesson.LessonRequest;
import real.talk.model.entity.User;
import real.talk.repository.user.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    public User saveUser(LessonRequest lessonRequest) {
        if (userRepository.existsByEmail(lessonRequest.getEmail())) {
            throw new IllegalArgumentException("User with email " + lessonRequest.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(lessonRequest.getName());
        user.setSubmissionTime(Instant.now());
        user.setEmail(lessonRequest.getEmail());
        user.setTelegram(lessonRequest.getTelegram());
        user.setLanguageLevel(lessonRequest.getLanguageLevel());
        user.setGrammarTopics(lessonRequest.getGrammarTopics());

        userRepository.save(user);
        return user;
    }
}
