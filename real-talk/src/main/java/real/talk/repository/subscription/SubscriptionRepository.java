package real.talk.repository.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import real.talk.model.entity.Subscription;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByPaddleSubscriptionId(String paddleSubscriptionId);

    List<Subscription> findByUserUserId(UUID userId);
}
