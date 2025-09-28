package real.talk.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TelegramAuthService {

    @Value("${telegram.bot-token}")
    private String botToken;

    public boolean checkTelegramAuthorization(Map<String, String> data) throws Exception {
        String hash = data.get("hash");
        String dataCheckString = data.entrySet().stream()
                .filter(e -> !e.getKey().equals("hash"))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n"));

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                MessageDigest.getInstance("SHA-256").digest(botToken.getBytes()), "HmacSHA256");
        mac.init(keySpec);

        byte[] hmac = mac.doFinal(dataCheckString.getBytes());
        String calculatedHash = bytesToHex(hmac);
        return calculatedHash.equals(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
