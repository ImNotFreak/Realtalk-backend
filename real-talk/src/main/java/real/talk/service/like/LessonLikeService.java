package real.talk.service.like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import real.talk.model.dto.like.LessonLikeResponse;
import real.talk.model.entity.Lesson;
import real.talk.model.entity.User;
import real.talk.model.entity.UserLessonLike;
import real.talk.repository.lesson.LessonRepository;
import real.talk.repository.like.LessonLikeRepository;
import real.talk.repository.user.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonLikeService {

    private final LessonLikeRepository lessonLikeRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public void likeLesson(UUID userId, UUID lessonId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        if (!lessonLikeRepository.existsByUserAndLesson(user, lesson)) {
            UserLessonLike like = new UserLessonLike();
            like.setUser(user);
            like.setLesson(lesson);
            lessonLikeRepository.save(like);
        }
    }

    @Transactional
    public void unlikeLesson(UUID userId, UUID lessonId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        lessonLikeRepository.deleteByUserAndLesson(user, lesson);
    }

    @Transactional(readOnly = true)
    public long getTotalLikes(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return lessonLikeRepository.countByUser(user);
    }

    public boolean isLiked(User user, Lesson lesson) {
        return lessonLikeRepository.existsByUserAndLesson(user, lesson);
    }

    public List<UUID> getLikedLessonIds(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return lessonLikeRepository.findAllByUser(user)
                .stream()
                .map(like -> like.getLesson().getId())
                .collect(Collectors.toList());
    }

    public List<LessonLikeResponse> getLikedLessons(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return lessonLikeRepository.findAllWithLessonByUser(user)
                .stream()
                .map(like -> {
                    var lesson = like.getLesson();
                    return new LessonLikeResponse(
                            lesson.getId(),
                            lesson.getYoutubeUrl(),
                            lesson.getLessonTopic(),
                            lesson.getTags(),
                            like.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }
}
