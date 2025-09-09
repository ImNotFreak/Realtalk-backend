package real.talk.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import real.talk.model.entity.User;
import real.talk.model.entity.enums.UserRole;
import real.talk.service.auth.JwtService;
import real.talk.service.user.UserService;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserService userService;
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .successHandler((request, response, authentication) -> {
                            OAuth2User principal = (OAuth2User) authentication.getPrincipal();
                            String email = principal.getAttribute("email");
                            String name = principal.getAttribute("name");

                            if (email == null) {
                                response.sendError(400, "Email not provided by Google");
                                return;
                            }

                            // Найти пользователя или создать
                            User user = userService.getUserByEmail(email)
                                    .orElseGet(() -> {
                                        User u = new User();
                                        u.setEmail(email);
                                        u.setName(name);
                                        u.setRole(UserRole.USER); // по умолчанию USER
                                        return u;
                                    });

                            userService.saveUser(user);

                            // Генерация JWT
                            String token = jwtService.generateToken(user.getEmail(), user.getRole());

                            // Редирект на фронт с токеном
                            response.sendRedirect(frontendUrl + "/login/success?token=" + token);
                        })
                );

        return http.build();
    }
}
