package real.talk.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import real.talk.model.entity.enums.UserRole;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;


    public String generateToken(String email, UserRole role) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .claim("role", role.name())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600 * 24)) // 24 часа
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
