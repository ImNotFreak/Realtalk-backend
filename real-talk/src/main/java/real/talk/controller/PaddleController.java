package real.talk.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import real.talk.model.entity.Subscription;
import real.talk.model.entity.User;
import real.talk.service.paddle.PaddleService;

@Slf4j
@RestController
@RequestMapping("/api/v1/paddle")
@RequiredArgsConstructor
public class PaddleController {

    private final PaddleService paddleService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = "Paddle-Signature", required = false) String signature,
            @RequestBody String payload) {
        if (signature == null) {
            log.error("Missing Paddle-Signature header");
            return ResponseEntity.badRequest().build();
        }

        try {
            boolean success = paddleService.processWebhook(signature, payload);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(401).build(); // Signature failed
            }
        } catch (Exception e) {
            log.error("Error processing Paddle webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/portal-session")
    public ResponseEntity<String> getPortalSession(
            @AuthenticationPrincipal User user) {
        String portalUrl = paddleService.getPortalSession(user);
        if (portalUrl != null) {
            return ResponseEntity.ok(portalUrl);
        } else {
            return ResponseEntity.status(500).body("Could not generate portal session");
        }
    }
}
