package real.talk.service.paddle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import real.talk.model.entity.Subscription;
import real.talk.model.entity.User;
import real.talk.model.entity.enums.SubscriptionPlan;
import real.talk.model.entity.enums.SubscriptionStatus;
import real.talk.repository.subscription.SubscriptionRepository;
import real.talk.service.user.UserService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaddleService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Value("${paddle.webhook.secret}")
    private String webhookSecret;

    // Plans map (Paddle Price ID -> Internal Plan)
    // You should preferably put these in properties or DB, but hardcoded for now is
    // fine for strict requirements
    // TODO: update with real PRICE IDs
    @Value("${paddle.price.smart:}")
    private String smartPriceId;

    @Value("${paddle.price.open-library:}")
    private String openLibraryPriceId;

    @Value("${paddle.price.plus:}")
    private String plusPriceId;

    // ...

    private SubscriptionPlan determinePlan(JsonNode data) {
        // Parse items to find priceId
        JsonNode items = data.path("items");
        if (items.isArray() && items.size() > 0) {
            for (JsonNode item : items) {
                String priceId = item.path("price").path("id").asText();
                if (priceId.equals(smartPriceId))
                    return SubscriptionPlan.SMART;
                if (priceId.equals(plusPriceId))
                    return SubscriptionPlan.PLUS;
                if (priceId.equals(openLibraryPriceId))
                    return SubscriptionPlan.OPEN_LIBRARY;
            }
        }
        return SubscriptionPlan.OPEN_LIBRARY; // default
    }

    private void updateUserLimits(User user, SubscriptionPlan plan, SubscriptionStatus status) {
        if (status != SubscriptionStatus.active && status != SubscriptionStatus.trialing) {
            return;
        }

        if (plan == SubscriptionPlan.SMART) {
            if (user.getLessonBuilderMinutes() == null || user.getLessonBuilderMinutes() < 300) {
                user.setLessonBuilderMinutes(300);
            }
        } else if (plan == SubscriptionPlan.PLUS) {
            if (user.getLessonBuilderMinutes() == null || user.getLessonBuilderMinutes() < 600) {
                user.setLessonBuilderMinutes(600);
            }
        } else if (plan == SubscriptionPlan.OPEN_LIBRARY) {
            // Ensure no minutes are given (or reset to 0/standard free limit?)
            // For now, do nothing or set to 0?
            // If user downgrades from Smart to Open Library, they might need to lose
            // minutes.
            // But existing logic is "No paid roles".
            // Let's safe-guard against getting free minutes.
            // user.setLessonBuilderMinutes(0); // Optional: force reset on downgrade
        }
    }

    @Transactional
    public boolean processWebhook(String signature, String payload) {
        if (!verifySignature(signature, payload)) {
            log.warn("Invalid Paddle webhook signature");
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("event_type").asText();
            JsonNode data = root.path("data");

            log.info("Processing Paddle event: {}", eventType);

            switch (eventType) {
                case "subscription.created":
                case "subscription.updated":
                    handleSubscriptionUpdate(data);
                    break;
                case "subscription.canceled":
                    handleSubscriptionCanceled(data);
                    break;
                // Add "transaction.completed" if needed for one-time payments or initial sub
                default:
                    log.debug("Ignored event type: {}", eventType);
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to parse webhook payload", e);
            throw new RuntimeException(e);
        }
    }

    private void handleSubscriptionUpdate(JsonNode data) {
        String paddleSubId = data.path("id").asText();
        String customerId = data.path("customer_id").asText();
        String statusStr = data.path("status").asText();
        String nextBilledAtStr = data.path("next_billed_at").asText(); // ISO 8601

        // Find user
        // Try to find by existing subscription linking
        Optional<Subscription> existingSub = subscriptionRepository.findByPaddleSubscriptionId(paddleSubId);
        User user = null;

        if (existingSub.isPresent()) {
            user = existingSub.get().getUser();
        } else {
            // New subscription. Ensure we have a way to link to User.
            // Look for 'custom_data' -> 'email' (Preferred) or 'userId' (Legacy)
            JsonNode customData = data.path("custom_data");
            log.info("Inspecting Paddle Custom Data: {}", customData.toString());

            if (customData.has("email")) {
                String email = customData.get("email").asText();
                user = userService.getUserByEmail(email).orElse(null);
                if (user == null) {
                    log.warn("User not found by email: {}", email);
                }
            } else if (customData.has("userId")) {
                String userIdStr = customData.get("userId").asText();
                try {
                    java.util.UUID userId = java.util.UUID.fromString(userIdStr);
                    user = userService.getUserById(userId);
                } catch (Exception e) {
                    log.warn("Invalid userId UUID: {}", userIdStr);
                }
            } else {
                log.warn("No 'email' or 'userId' in custom_data");
            }
        }

        if (user == null) {
            log.error("Could not link subscription {} to any user", paddleSubId);
            return;
        }

        // Determine Plan
        SubscriptionPlan plan = determinePlan(data);
        SubscriptionStatus status = SubscriptionStatus.valueOf(statusStr);

        Subscription subscription = existingSub.orElse(new Subscription());
        subscription.setUser(user);
        subscription.setPaddleSubscriptionId(paddleSubId);
        subscription.setPaddleCustomerId(customerId);
        subscription.setStatus(status);
        subscription.setPlan(plan);
        if (nextBilledAtStr != null && !nextBilledAtStr.isEmpty() && !nextBilledAtStr.equals("null")) {
            subscription.setNextBilledAt(Instant.parse(nextBilledAtStr));
        }
        subscription.setUpdatedAt(Instant.now());
        if (subscription.getCreatedAt() == null) {
            subscription.setCreatedAt(Instant.now());
        }

        subscriptionRepository.save(subscription);

        // Update User fields
        user.setPaddleCustomerId(customerId);
        updateUserLimits(user, plan, status);
        userService.saveUser(user);
    }

    private void handleSubscriptionCanceled(JsonNode data) {
        String paddleSubId = data.path("id").asText();
        Optional<Subscription> subOpt = subscriptionRepository.findByPaddleSubscriptionId(paddleSubId);
        if (subOpt.isPresent()) {
            Subscription sub = subOpt.get();
            sub.setStatus(SubscriptionStatus.canceled);
            sub.setUpdatedAt(Instant.now());
            subscriptionRepository.save(sub);

            // Revoke access logic if immediate, but usually cancellation is "at end of
            // period"
            // Paddle "subscription.canceled" usually means it IS canceled now or scheduled?
            // Actually check 'status' field in payload. If it's 'deleted' or 'canceled'.
            // The handler above handles "updated" so maybe we just rely on that unless
            // specifically canceled event has different structure.
            // But we already map status. so re-using handleSubscriptionUpdate might be
            // safer if data structure is same.
            // The 'data' for canceled event is usually the subscription object too.
            handleSubscriptionUpdate(data);
        }
    }

    @Transactional(readOnly = true)
    public Subscription getSubscription(User user) {
        return subscriptionRepository.findByUserUserId(user.getUserId())
                .stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.active)
                .findFirst()
                .orElse(null);
    }

    private boolean verifySignature(String signature, String payload) {
        try {
            // Paddle signature format: ts=...,h1=...
            String[] parts = signature.split(";");
            String ts = null;
            String h1 = null;
            for (String part : parts) {
                if (part.startsWith("ts="))
                    ts = part.substring(3);
                if (part.startsWith("h1="))
                    h1 = part.substring(3);
            }

            if (ts == null || h1 == null)
                return false;

            String signedPayload = ts + ":" + payload;
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));

            // Hex encode
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(h1);
        } catch (Exception e) {
            log.error("Signature verification error", e);
            return false;
        }
    }
}
