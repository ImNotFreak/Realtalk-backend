package real.talk.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import real.talk.model.dto.folder.*;
import real.talk.service.folder.FolderLessonService;
import real.talk.service.folder.FolderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/folders")
class FolderController {
    private final FolderService folderService;
    private final FolderLessonService folderLessonService;

    // Получить все папки пользователя
    @GetMapping("")
    public ResponseEntity<List<FolderResponse>> getFolders(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        return ResponseEntity.ok(folderService.getUserFolders(userId));
    }

    // Создать папку
    @PostMapping("")
    public ResponseEntity<FolderCreateResponse> createFolder(@AuthenticationPrincipal Jwt jwt,
                                                             @RequestBody FolderCreateRequest folderCreateRequest) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        return ResponseEntity.ok(folderService.createFolder(userId, folderCreateRequest.getName()));
    }

    // Переименовать папку
    @PutMapping("/{folderId}")
    public ResponseEntity<FolderRenameResponse> renameFolder(
                                               @PathVariable Long folderId,
                                               @RequestBody FolderRenameRequest folderRenameRequest) {
        return ResponseEntity.ok(folderService.renameFolder(folderId, folderRenameRequest.getName()));
    }

    // Удалить папку
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        folderService.deleteFolder(folderId);
        return ResponseEntity.noContent().build();
    }

    // Добавить урок в папку
    @PostMapping("/{folderId}/lessons")
    public ResponseEntity<Void> addLesson(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable Long folderId,
                                          @RequestBody AddLessonToFolderRequest addLessonToFolderRequest) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        folderLessonService.addLessonToFolder(userId, folderId, addLessonToFolderRequest.lessonId());
        return ResponseEntity.ok().build();
    }

    // Удалить урок из папки
    @DeleteMapping("/{folderId}/lessons/{lessonId}")
    public ResponseEntity<Void> removeLesson(@AuthenticationPrincipal Jwt jwt,
                                             @PathVariable Long folderId,
                                             @PathVariable UUID lessonId) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        folderLessonService.removeLessonFromFolder(userId, folderId, lessonId);
        return ResponseEntity.noContent().build();
    }

    // Получить все уроки в папке
    @GetMapping("/{folderId}/lessons")
    public ResponseEntity<List<FolderLessonResponse>> getLessons(@AuthenticationPrincipal Jwt jwt,
                                                                 @PathVariable Long folderId) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        return ResponseEntity.ok(folderLessonService.getLessonsInFolder(folderId, userId));
    }
}
