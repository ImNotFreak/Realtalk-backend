package real.talk.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import real.talk.model.entity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
}
