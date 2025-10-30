package real.talk.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;
import real.talk.model.entity.User;
import real.talk.model.entity.enums.UserRole;
import real.talk.repository.whitelist.WhiteListRepository;
import real.talk.service.auth.JwtAuthenticationFilter;
import real.talk.service.auth.JwtService;
import real.talk.service.user.UserService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserService userService;
    private final WhiteListRepository whiteListRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ====== API CHAIN (закрытая, 401 вместо редиректа) ======
    @Bean
    @Order(1)
    SecurityFilterChain apiChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                // ВАЖНО: для API вместо редиректа отдаём 401
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);;

        return http.build();
    }

    // ====== WEB CHAIN (OAuth2, редиректы на фронт) ======
    @Bean
    @Order(2)
    SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                            if (!whiteListRepository.existsByEmail(email)) {
                                response.sendError(401, "Access denied: you are not allowed to access this resource");
                                return;
                            }

                            User user = userService.getUserByEmail(email).orElseGet(() -> {
                                User u = new User();
                                u.setEmail(email);
                                u.setName(name);
                                u.setOrderNumber(UUID.randomUUID());
                                u.setLessonCount(1);
                                u.setDuration(0.0);
                                u.setCreatedAt(Instant.now());
                                u.setRole(UserRole.USER);
                                return u;
                            });
                            userService.saveUser(user);

                            String token = jwtService.generateToken(user.getEmail(), user.getName(), user.getRole());
                            response.sendRedirect(frontendUrl + "/login/success?token=" + token);
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(frontendUrl));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","X-Requested-With","Origin"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        return source;
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}