package real.talk.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import real.talk.model.dto.user.UserAccessDto;
import real.talk.model.entity.User;
import real.talk.service.access.AccessControlService;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final AccessControlService accessControlService;

    @GetMapping("/me")
    public ResponseEntity<UserAccessDto> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        UserAccessDto dto = accessControlService.getUserAccessDto(user);
        return ResponseEntity.ok(dto);
    }
}
