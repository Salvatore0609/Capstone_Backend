package it.epicode.Capstone.login.utentigoogle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtenteGoogleRepository extends JpaRepository<UtenteGoogle, Long> {
    Optional<UtenteGoogle> findByEmail(String email);
}
