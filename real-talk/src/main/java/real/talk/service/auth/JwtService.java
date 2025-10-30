package real.talk.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import real.talk.model.entity.enums.UserRole;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${app.jwt.expiration-seconds:604800}")
    private long expirationSeconds;

    public String generateToken(String email, String name, UserRole role) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .claim("role", role.name())
                .claim("email", email)
                .claim("name", name)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(expirationSeconds)) // 24 часа
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String extractEmail(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        String sub = jwt.getSubject();
        if (sub != null && !sub.isBlank()) return sub;
        Object email = jwt.getClaims().get("email");
        return email != null ? String.valueOf(email) : null;
    }

    public String extractRole(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        Object role = jwt.getClaims().get("role");
        return role != null ? String.valueOf(role) : null;
    }

    public boolean isTokenValid(String token) {
        try {
            jwtDecoder.decode(token); // бросит исключение, если подпись/exp невалидны
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

}
