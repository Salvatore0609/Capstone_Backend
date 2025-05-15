package it.epicode.Capstone.login.utenti;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UtenteResponse {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String nome;
    private String cognome;
    private String avatar;
    private Integer annoNascita;
    private String luogoNascita;
    private String residenza;
    private String nomeCompagnia;
    private String lingua;
}
