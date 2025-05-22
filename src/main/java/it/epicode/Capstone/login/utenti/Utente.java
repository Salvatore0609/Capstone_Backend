package it.epicode.Capstone.login.utenti;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.epicode.Capstone.login.auth.Role;
import it.epicode.Capstone.login.utenti.MyProject.Project;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Utenti")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utente implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column
    private String username;

    @Column
    @ToString.Exclude
    @JsonIgnore
    private String password;

    @Column( unique = true, length = 100)
    private String email;
    @Column( length = 50)
    private String nome;
    @Column (length = 50)
    private String cognome;
    @Column(length = 500)
    private String avatar;
    @Column
    private LocalDate dataNascita;
    @Column
    private String luogoNascita;
    @Column
    private String residenza;
    @Column
    private String nomeCompagnia;
    @Column
    private String lingua;


    //tipo utente
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;


    //per le autorizzazioni
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) () -> role.name())
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    private  boolean accountNonExpired=true;
    private  boolean accountNonLocked=true;
    private  boolean credentialsNonExpired=true;
    private  boolean enabled=true;
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}