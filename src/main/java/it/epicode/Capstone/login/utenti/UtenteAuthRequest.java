package it.epicode.Capstone.login.utenti;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtenteAuthRequest {
    @NotBlank(message = "Il nome non può essere vuoto")
    private String nome;
    @NotBlank (message = "Il cognome non può essere vuoto")
    private String cognome;
    @NotBlank (message = "L'email non può essere vuota")
    private String email;
    @NotBlank (message = "L'username non può essere vuoto")
    private String username;
    @NotBlank (message = "La password non può essere vuota")
    private String password;
    @NotNull(message = "L'anno di nascita non può essere vuoto")
    private LocalDate dataNascita;
    @NotBlank (message = "Il tuo  luogo di nascita non può essere vuoto")
    private String luogoNascita;
    @NotBlank (message = "Il tuo domicilio attuale non può essere vuoto")
    private String residenza;

    private String nomeCompagnia;
    private String lingua;
}
