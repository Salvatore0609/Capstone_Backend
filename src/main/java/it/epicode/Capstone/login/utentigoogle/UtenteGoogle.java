package it.epicode.Capstone.login.utentigoogle;

import it.epicode.Capstone.auth.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "UtenteGoogle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtenteGoogle {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String nome;

    private String avatar;


    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    /*nuovo*/
    @Column(length = 512)
    private String accessToken;

    @Column(length = 512)
    private String refreshToken;

    private Instant tokenExpiry;
}
