package it.epicode.Capstone.login.utenti;

import it.epicode.Capstone.auth.JwtTokenUtil;
import it.epicode.Capstone.auth.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UtenteRunner implements CommandLineRunner {
    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public void run(String... args) throws Exception {
        Utente admin = new Utente();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("adminpwd"));
        admin.setRoles(Set.of(Role.ROLE_ADMIN));
        utenteRepository.save(admin);
    }
}
