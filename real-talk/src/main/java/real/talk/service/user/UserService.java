package real.talk.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import real.talk.model.dto.lesson.LessonRequest;
import real.talk.model.entity.User;
import real.talk.repository.user.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    public User saveUser(LessonRequest lessonRequest) {
        Optional<User> findByEmail = userRepository.findAllByEmail(lessonRequest.getEmail());
        if (findByEmail.isPresent())  {
            return findByEmail.get();
        }

        User user = new User();
        user.setName(lessonRequest.getName());
        user.setOrderNumber(UUID.randomUUID());
        user.setSubmissionTime(Instant.now());
        user.setEmail(lessonRequest.getEmail());
        user.setTelegram(lessonRequest.getTelegram());

        userRepository.save(user);
        return user;
    }
}
