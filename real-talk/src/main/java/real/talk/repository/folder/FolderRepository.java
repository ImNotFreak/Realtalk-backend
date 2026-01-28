package real.talk.repository.folder;

import org.springframework.data.jpa.repository.JpaRepository;
import real.talk.model.entity.User;
import real.talk.model.entity.Folder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findAllByUser(User user);
    Optional<Folder> findByIdAndUser(Long id, User user);
    Optional<Folder> findByUserAndName(User user, String name);
}
