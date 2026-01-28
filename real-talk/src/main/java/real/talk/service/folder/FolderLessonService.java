package real.talk.service.folder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import real.talk.model.dto.folder.FolderLessonResponse;
import real.talk.model.entity.Folder;
import real.talk.model.entity.FolderLesson;
import real.talk.model.entity.Lesson;
import real.talk.repository.folder.FolderLessonRepository;
import real.talk.repository.folder.FolderRepository;
import real.talk.repository.lesson.LessonRepository;
import real.talk.repository.user.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FolderLessonService {
    private final FolderLessonRepository folderLessonRepository;
    private final LessonRepository lessonRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public void addLessonToFolder(UUID userId, Long folderId, UUID lessonId) {
        Folder folder = folderRepository.findById(folderId)
                .filter(f -> f.getUser().getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Folder not found or access denied"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        if (folderLessonRepository.findByFolderAndLesson(folder, lesson).isPresent()) {
            throw new RuntimeException("Lesson already in folder");
        }
        FolderLesson fl = new FolderLesson();
        fl.setFolder(folder);
        fl.setLesson(lesson);
        fl.setCreatedAt(Instant.now());
        folderLessonRepository.save(fl);
    }

    public void removeLessonFromFolder(UUID userId, Long folderId, UUID lessonId) {
        Folder folder = folderRepository.findById(folderId)
                .filter(f -> f.getUser().getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Folder not found or access denied"));

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        folderLessonRepository.findByFolderAndLesson(folder, lesson)
                .ifPresent(folderLessonRepository::delete);
    }

    public List<FolderLessonResponse> getLessonsInFolder(Long folderId, UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Folder folder = folderRepository.findByIdAndUser(folderId, user)
                .orElseThrow(() -> new RuntimeException("Folder not found or access denied"));

        return folderLessonRepository.findLessonsByFolder(folder);
    }
}
