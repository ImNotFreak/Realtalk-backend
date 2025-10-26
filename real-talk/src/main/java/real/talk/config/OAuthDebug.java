package real.talk.config;

import jakarta.annotation.PostConstruct;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Component
public class OAuthDebug {

    private final ClientRegistrationRepository clientRegistrationRepository;

    public OAuthDebug(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @PostConstruct
    public void logRedirectUri() {
        var registration = clientRegistrationRepository.findByRegistrationId("google");
        System.out.println("Google redirect-uri: " + registration.getRedirectUri());
    }
}
