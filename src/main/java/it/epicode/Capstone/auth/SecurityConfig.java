package it.epicode.Capstone.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.epicode.Capstone.login.utentigoogle.CustomOAuth2UserService;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogle;
import it.epicode.Capstone.login.utentigoogle.UtenteGoogleRepository;
import it.epicode.Capstone.common.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
import java.util.Optional;
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

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UtenteGoogleRepository utenteGoogleRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    // Valore base per la redirect URL dopo il login Google
    @Value("${app.redirect-url}")
    private String redirectUrlBase;

    // Configurazione del filtro di sicurezza per le richieste OAuth2 (Google)
    @Bean
    @Order(1)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Applica questa configurazione solo a percorsi OAuth2
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())             // Disabilita CSRF per OAuth2
                .formLogin(form -> form.disable())        // Disabilita il form login tradizionale
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/google")      // URL di inizio flusso OAuth2
                        .authorizationEndpoint(authz ->
                                authz.baseUri("/oauth2/authorization"))    // Base URI per autorizzazione
                        .redirectionEndpoint(redir ->
                                redir.baseUri("/login/oauth2/code/*"))     // Base URI per callback Google
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)) // Servizio per caricare dati utente
                        .successHandler(this::oauth2LoginSuccessHandler)   // Handler custom per successo login
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()   // Permetti tutte le richieste su questi percorsi
                );

        return http.build();
    }

    // Configurazione del filtro di sicurezza per le richieste JWT (tutte le altre)
    @Bean
    @Order(2)
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Applica questa configurazione a tutte le richieste non gestite dal filtro OAuth2
                .securityMatcher(request -> true)
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())    // Disabilita CSRF per le API stateless
                .authorizeHttpRequests(auth -> auth
                        // Percorsi pubblici accessibili senza autenticazione
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
                        .anyRequest().authenticated()  // Tutte le altre richiedono autenticazione
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(jwtAuthenticationEntryPoint) // Gestione errori auth
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Sessione stateless
                )
                // Aggiunge il filtro JWT prima del filtro di autenticazione standard
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                // Imposta il provider per l’autenticazione con username/password
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    // Espone il Bean AuthenticationManager, necessario per login form (username/password)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Bean per il password encoder, in questo caso BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configura il provider di autenticazione basato su DAO (UserDetailsService + PasswordEncoder)
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(customUserDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    // Handler invocato al successo del login OAuth2 (Google)
    private void oauth2LoginSuccessHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.Authentication authentication
    ) throws IOException {
        // Cast al token specifico OAuth2
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // es. "google"
        String principalName = oauthToken.getName(); // email o ID utente

        // Carica l'OAuth2AuthorizedClient per ottenere i token (access e refresh)
        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient(registrationId, principalName);
        if (authorizedClient == null) {
            throw new IllegalStateException("AuthorizedClient non trovato per utente: " + principalName);
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

        String accessTokenValue = accessToken.getTokenValue();
        Instant expiresAt = accessToken.getExpiresAt();
        String refreshTokenValue = (refreshToken != null) ? refreshToken.getTokenValue() : null;

        // Creazione o recupero dell'utente Google (gestione transazionale con retry)
        UtenteGoogle utenteGoogle = handleUserCreation(oauthToken, principalName);

        // Salvataggio o aggiornamento dei token OAuth2 nel DB
        updateUserTokens(utenteGoogle, accessTokenValue, refreshTokenValue, expiresAt);

        // Generazione del JWT per il nostro sistema e redirect al front-end
        generateJwtAndRedirect(response, utenteGoogle);
    }

    // Metodo transazionale per gestire la creazione o il recupero dell'UtenteGoogle
    @Transactional
    protected UtenteGoogle handleUserCreation(OAuth2AuthenticationToken oauthToken, String email) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                // Controlla se esiste già un utente con la stessa email
                Optional<UtenteGoogle> existingUser = utenteGoogleRepository
                        .findByEmail(oauthToken.getPrincipal().getAttribute("email"));

                if (existingUser.isPresent()) {
                    log.info("Utente Google esistente: {}", email);
                    return existingUser.get();
                } else {
                    // Se non esiste, estrai i dati da OAuth2User e crea un nuovo UtenteGoogle
                    OAuth2User oauth2User = oauthToken.getPrincipal();
                    UtenteGoogle newUser = new UtenteGoogle();
                    newUser.setEmail(oauth2User.getAttribute("email"));
                    newUser.setNome(oauth2User.getAttribute("name"));
                    newUser.setAvatar(oauth2User.getAttribute("picture"));
                    newUser.setRoles(Set.of(Role.ROLE_USER));

                    UtenteGoogle savedUser = utenteGoogleRepository.save(newUser);
                    log.info("Nuovo utente Google creato: {}", savedUser);

                    // Invia una mail di benvenuto in modo asincrono (non blocca il flusso)
                    new Thread(() -> sendWelcomeEmail(savedUser)).start();

                    return savedUser;
                }
            } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException ex) {
                // Gestione del conflitto di concorrenza: riprova fino a maxRetries
                attempt++;
                log.warn("Conflitto di concorrenza (tentativo {} di {}): {}", attempt, maxRetries, ex.getMessage());

                if (attempt >= maxRetries) {
                    log.error("Fallito dopo {} tentativi. Recupero forzato utente.", maxRetries);
                    return utenteGoogleRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException(
                                    "Utente non trovato dopo conflitto: " + email));
                }

                // Breve pausa prima di riprovare
                try {
                    Thread.sleep(100 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new IllegalStateException("Flusso di creazione utente non completato");
    }

    // Invia una mail di benvenuto al nuovo utente
    private void sendWelcomeEmail(UtenteGoogle user) {
        try {
            emailSenderService.sendEmail(
                    user.getEmail(),
                    "Benvenuto",
                    "Ciao " + user.getNome() + ", benvenuto in Archiplanner!"
            );
            log.info("Email di benvenuto inviata a: {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Invio email fallito: {}", e.getMessage());
        }
    }

    // Aggiorna i token OAuth2 (access e refresh) per l'utente e salva nel repository
    private void updateUserTokens(UtenteGoogle user, String accessToken, String refreshToken, Instant expiry) {
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setTokenExpiry(expiry);
        utenteGoogleRepository.save(user);
        log.debug("Token aggiornati per utente: {}", user.getEmail());
    }

    // Genera un JWT per l'utente e redirige al front-end con i dati codificati in Base64
    private void generateJwtAndRedirect(HttpServletResponse response, UtenteGoogle user) throws IOException {
        // Costruzione dei dettagli dell'utente per il JWT
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                "", // Password vuota perché usiamo OAuth2
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList())
        );

        // Genera il token JWT
        String jwt = jwtTokenUtil.generateToken(userDetails);

        // Prepara il payload JSON da inviare al front-end
        Map<String, String> payload = Map.of(
                "token", jwt,
                "id", user.getId().toString(),
                "username", user.getEmail(),
                "email", user.getEmail(),
                "nome", user.getNome(),
                "avatar", user.getAvatar()
        );

        // Serializza in JSON e poi codifica in Base64 per passarlo come parametro nella URL
        String json = new ObjectMapper().writeValueAsString(payload);
        String b64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        String encoded = URLEncoder.encode(b64, StandardCharsets.UTF_8);

        // Costruisci la URL di redirect con i dati codificati
        String redirectUrl = redirectUrlBase + "/login-google-success?data=" + encoded;

        // Effettua il redirect al client
        response.sendRedirect(redirectUrl);
    }
}
