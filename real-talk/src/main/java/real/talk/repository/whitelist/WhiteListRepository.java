package real.talk.repository.whitelist;

import org.springframework.data.jpa.repository.JpaRepository;
import real.talk.model.entity.WhiteList;

public interface WhiteListRepository extends JpaRepository<WhiteList, String> {

    boolean existsByEmail(String email);
}
