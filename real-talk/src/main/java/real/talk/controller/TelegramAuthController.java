package real.talk.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import real.talk.model.entity.User;
import real.talk.model.entity.enums.UserRole;
import real.talk.service.auth.JwtService;
import real.talk.service.auth.TelegramAuthService;
import real.talk.service.user.UserService;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TelegramAuthController {

    private final TelegramAuthService telegramAuthService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/telegram")
    public ResponseEntity<?> telegramLogin(@RequestBody Map<String, String> data) throws Exception {
        if (!telegramAuthService.checkTelegramAuthorization(data)) {
            return ResponseEntity.status(401).body("Invalid telegram data");
        }

        String telegramId = data.get("id");
        String firstName = data.get("first_name");
        String lastName = data.get("last_name");     // может быть null
        String username = data.get("username"); // может быть null

        User user = userService.getUserByTelegramId(telegramId).orElseGet(() -> {
            User u = new User();
            u.setTelegramId(telegramId);
            u.setTelegramName(username != null ? username : telegramId);
            u.setName(firstName + (lastName != null ? " " + lastName : ""));
            u.setLessonCount(0);
            u.setDuration(0.0);
            u.setCreatedAt(Instant.now());
            u.setRole(UserRole.USER); // по умолчанию USER
            return userService.saveUser(u);
        });

        String token = jwtService.generateTelegramToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
