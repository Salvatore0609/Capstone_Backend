package it.epicode.Capstone.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.epicode.Capstone.login.utentigoogle.CustomOAuth2UserService;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogle;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogleRepository;
import it.epicode.Capstone.common.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    /* iniettiamo il service per recuperare accessToken/refreshToken */
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    /* ---------------------------------------------------------------- */

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UtenteGoogleRepository utenteGoogleRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    @Value("${app.redirect-url}")
    private String redirectUrlBase;


    @Bean
    @Order(1)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Questo chain gira solo per le URL di OAuth2/login
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .oauth2Login(oauth2 -> oauth2
                                .loginPage("/oauth2/authorization/google")
                                .authorizationEndpoint(authz -> authz.baseUri("/oauth2/authorization"))
                                .redirectionEndpoint(redir -> redir.baseUri("/login/oauth2/code/*"))
                                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                                /* REPLACED: inline successHandler con metodo dedicato */
                                .successHandler(this::oauth2LoginSuccessHandler)
                        /* ---------------------------------------------------------------- */
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Questo chain gira su tutte le altre richieste
                .securityMatcher(request -> true)
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/utenti/login",
                                "/utenti/register",
                                "/oauth2/**",
                                "/geocode",
                                "/sottozone",
                                "/articoli",
                                "/articoli/**",
                                "/ws/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // solo per autenticazione “normale” (username/password)
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(customUserDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }


    /* NEW: metodo dedicato per il success handler OAuth2 */
    private void oauth2LoginSuccessHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.Authentication authentication
    ) throws IOException {
        // 1) Ottengo il token di autenticazione OAuth2
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // "google"
        String principalName = oauthToken.getName();  // di solito l'email

        // 2) Recupero l'OAuth2AuthorizedClient per avere accessToken e refreshToken
        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient(registrationId, principalName);
        if (authorizedClient == null) {
            throw new IllegalStateException("AuthorizedClient non trovato per utente: " + principalName);
        }

        // 3) Estrazione dei token
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

        String accessTokenValue = accessToken.getTokenValue();
        Instant expiresAt = accessToken.getExpiresAt();
        String refreshTokenValue = (refreshToken != null) ? refreshToken.getTokenValue() : null;

        // 4) Recupero o creo UtenteGoogle in DB
        UtenteGoogle utenteGoogle = utenteGoogleRepository.findByEmail(principalName)
                .orElseGet(() -> {
                    // Se non esiste, creo un nuovo UtenteGoogle
                    OAuth2User oauth2User = oauthToken.getPrincipal();
                    UtenteGoogle newUser = new UtenteGoogle();
                    newUser.setEmail(oauth2User.getAttribute("email"));
                    newUser.setNome(oauth2User.getAttribute("name"));
                    newUser.setAvatar(oauth2User.getAttribute("picture"));
                    newUser.setRoles(Set.of(Role.ROLE_USER));
                    log.info("Nuovo utente Google creato: {}", newUser);
                    try {
                        emailSenderService.sendEmail(
                                newUser.getEmail(),
                                "Benvenuto",
                                "Ciao " + newUser.getNome() + " Benvenuto in Archiplanner!"
                        );
                    } catch (MessagingException e) {
                        throw new RuntimeException(e);
                    }
                    return utenteGoogleRepository.save(newUser);
                });

        // 5) Salvo accessToken, refreshToken e scadenza in UtenteGoogle
        utenteGoogle.setAccessToken(accessTokenValue);
        utenteGoogle.setRefreshToken(refreshTokenValue);
        utenteGoogle.setTokenExpiry(expiresAt);
        utenteGoogleRepository.save(utenteGoogle);

        // 6) Genero JWT interno e redirect al frontend
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                utenteGoogle.getEmail(),
                "",
                utenteGoogle.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList())
        );
        String jwt = jwtTokenUtil.generateToken(userDetails);

        Map<String, String> payload = Map.of(
                "token", jwt,
                "id", utenteGoogle.getId().toString(),
                "username", utenteGoogle.getEmail(),
                "email", utenteGoogle.getEmail(),
                "nome", utenteGoogle.getNome(),
                "avatar", utenteGoogle.getAvatar()
        );
        String json = new ObjectMapper().writeValueAsString(payload);
        String b64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        String encoded = URLEncoder.encode(b64, StandardCharsets.UTF_8);
        String redirectUrl = redirectUrlBase + "/login-google-success?data=" + encoded;

        response.sendRedirect(redirectUrl);
    }
    /* ---------------------------------------------------------------- */
}
