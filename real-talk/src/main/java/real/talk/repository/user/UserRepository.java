package real.talk.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import real.talk.model.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findById(UUID uuid);
    Optional<User> findUserByEmail(String email);
    Optional<User> findByPaddleCustomerId(String paddleCustomerId);
}
