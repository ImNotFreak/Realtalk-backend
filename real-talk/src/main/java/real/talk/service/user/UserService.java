package real.talk.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import real.talk.model.dto.lesson.LessonCreateRequest;
import real.talk.model.entity.User;
import real.talk.model.entity.enums.UserRole;
import real.talk.repository.user.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    public User saveUser(LessonCreateRequest lessonRequest) {
        Optional<User> findByEmail = this.getUserByEmail(lessonRequest.getEmail());
        Optional<User> findByTelegram = this.getUserByTelegramId(lessonRequest.getEmail());

        if (findByEmail.isPresent())  {
            User user = findByEmail.get();
            if (user.getTelegramName() == null) {
                user.setTelegramName(lessonRequest.getTelegram());
                userRepository.save(user);
            }
            return user;
        }

        if (findByTelegram.isPresent())  {
            User user = findByTelegram.get();
            if (user.getEmail() == null) {
                user.setEmail(lessonRequest.getEmail());
                userRepository.save(user);
            }
            return user;
        }

        User user = new User();
        user.setName(lessonRequest.getName());
        user.setRole(UserRole.USER);
        user.setLessonCount(1);
        user.setDuration(0.0);
        user.setOrderNumber(UUID.randomUUID());
        user.setCreatedAt(Instant.now());
        user.setEmail(lessonRequest.getEmail());
        user.setTelegramName(lessonRequest.getTelegram());

        userRepository.save(user);
        return user;
    }

    public User saveUser(User user) {
        userRepository.save(user);
        return user;
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found!"));
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public Optional<User> getUserByTelegramId(String telegramId) {
        return userRepository.findUserByTelegramId(telegramId);
    }

    public Optional<User> getUserByTelegramName(String telegramName) {
        return userRepository.findUserByTelegramName(telegramName);
    }

}
