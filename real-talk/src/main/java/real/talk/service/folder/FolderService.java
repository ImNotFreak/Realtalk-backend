package real.talk.service.folder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import real.talk.model.dto.folder.FolderCreateResponse;
import real.talk.model.dto.folder.FolderRenameRequest;
import real.talk.model.dto.folder.FolderRenameResponse;
import real.talk.model.dto.folder.FolderResponse;
import real.talk.model.entity.Folder;
import real.talk.model.entity.User;
import real.talk.repository.folder.FolderLessonRepository;
import real.talk.repository.folder.FolderRepository;
import real.talk.repository.user.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final FolderLessonRepository folderLessonRepository;
    private final UserRepository userRepository;

    public FolderCreateResponse createFolder(UUID userId, String name) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Folder folder = new Folder();
        folder.setUser(user);
        folder.setName(name);
        folder.setCreatedAt(Instant.now());
        folder.setUpdatedAt(Instant.now());
        folderRepository.save(folder);
        return FolderCreateResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .build();
    }

    public FolderRenameResponse renameFolder(Long folderId, String newName) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
        folder.setName(newName);
        folder.setUpdatedAt(Instant.now());
        folderRepository.save(folder);
        return FolderRenameResponse.builder()
                .name(folder.getName())
                .id(folder.getId())
                .build();
    }

    public void deleteFolder(Long folderId) {
        folderRepository.deleteById(folderId);
    }

    public List<FolderResponse> getUserFolders(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<Folder> folders = folderRepository.findAllByUser(user);
        return folders.stream()
                .map(folder -> {
                    int count = folderLessonRepository.countByFolder(folder);
                    return FolderResponse.builder()
                            .id(folder.getId())
                            .name(folder.getName())
                            .lessonCount(count)
                            .build();
                }).toList();
    }

    public boolean folderNameExists(User user, String name) {
        return folderRepository.findByUserAndName(user, name).isPresent();
    }
}
