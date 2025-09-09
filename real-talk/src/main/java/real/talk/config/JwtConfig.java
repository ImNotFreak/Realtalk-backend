package real.talk.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtConfig {

    private final String jwtSecret = "YOUR_VERY_SECURE_JWT_SECRET_256_BITS_MIN";

    @Bean
    public JwtEncoder jwtEncoder() {
        byte[] keyBytes = jwtSecret.getBytes();
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

}