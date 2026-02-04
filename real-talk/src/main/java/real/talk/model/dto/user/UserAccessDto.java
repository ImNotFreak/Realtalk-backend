package real.talk.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import real.talk.model.entity.enums.SubscriptionPlan;
import real.talk.model.entity.enums.UserRole;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAccessDto {
    private String id;
    private String email;
    private String name;
    private UserRole role;
    private SubscriptionPlan plan;
    private Integer lessonBuilderMinutes;
    private Permissions permissions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Permissions {
        private boolean canAccessLibrary;
        private boolean canUseBuilder;
        private boolean canUseCollections;
        private boolean canUseScenarios;
    }
}
