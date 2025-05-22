package it.epicode.Capstone.login.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.epicode.Capstone.login.authGoogle.CustomOAuth2UserService;
import it.epicode.Capstone.login.authGoogle.UtenteGoogle;
import it.epicode.Capstone.login.authGoogle.UtenteGoogleRepository;
import it.epicode.Capstone.login.common.EmailSenderService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
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

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UtenteGoogleRepository utenteGoogleRepository;

    @Autowired
    private EmailSenderService emailSenderService;


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
                        .successHandler((request, response, authentication) -> {
                            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                            OAuth2User oauth2User = oauth2Token.getPrincipal();
                            String email = oauth2User.getAttribute("email");

                            // Recupera o crea l'utente Google
                            UtenteGoogle utenteGoogle = utenteGoogleRepository.findByEmail(email)
                                    .orElseGet(() -> {
                                        // Crea un nuovo utente se non esiste
                                        UtenteGoogle newUser = new UtenteGoogle();
                                        newUser.setEmail(email);
                                        newUser.setNome(oauth2User.getAttribute("name"));
                                        newUser.setAvatar(oauth2User.getAttribute("picture"));
                                        newUser.setRoles(Set.of(Role.ROLE_USER));
                                        log.info("Nuovo utente Google creato: {}", newUser);
                                        try {
                                            // Invio dell'email di benvenuto
                                            emailSenderService.sendEmail(newUser.getEmail(), "Benvenuto", "Ciao " + newUser.getNome() + " Benvenuto in Archiplanner!");
                                        } catch (MessagingException e) {
                                            throw new RuntimeException(e);
                                        }

                                        return utenteGoogleRepository.save(newUser);
                                    });

                            UserDetails userDetails = new UserDetails() {
                                @Override
                                public Collection<? extends GrantedAuthority> getAuthorities() {
                                    return utenteGoogle.getRoles().stream()
                                            .map(role -> new SimpleGrantedAuthority(role.name()))
                                            .collect(Collectors.toList());
                                }

                                @Override
                                public String getPassword() {
                                    return "";
                                }

                                @Override
                                public String getUsername() {
                                    return utenteGoogle.getEmail();
                                }

                                @Override public boolean isAccountNonExpired() { return true; }
                                @Override public boolean isAccountNonLocked() { return true; }
                                @Override public boolean isCredentialsNonExpired() { return true; }
                                @Override public boolean isEnabled() { return true; }
                            };

                            String jwt = jwtTokenUtil.generateToken(userDetails);
                            Map<String,String> payload = Map.of(
                                    "token", jwt,
                                    "id", utenteGoogle.getId().toString(),
                                    "username", utenteGoogle.getEmail(),
                                    "email", utenteGoogle.getEmail(),
                                    "nome", utenteGoogle.getNome(),
                                    "avatar", utenteGoogle.getAvatar()
                            );
                            ObjectMapper mapper = new ObjectMapper();
                            String json = mapper.writeValueAsString(payload);
                            String b64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
                            String encoded = URLEncoder.encode(b64, StandardCharsets.UTF_8);
                            String redirectUrl = "http://localhost:5173/login-google-success?data=" + encoded;
                            response.sendRedirect(redirectUrl);
                        })
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
                        .requestMatchers("/utenti/login", "/utenti/register",
                                "/oauth2/**", "/geocode").permitAll()
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

    // solo per autenticazione “normale” (username/password) se ti serve
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(customUserDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }
}
