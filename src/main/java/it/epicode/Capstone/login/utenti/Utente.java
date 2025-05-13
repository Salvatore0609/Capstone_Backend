package it.epicode.Capstone.login.utenti;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.epicode.Capstone.login.auth.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "Utente")
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

    @Column
    private String email;
    @Column
    private String nome;
    @Column
    private String cognome;
    @Column
    private String avatar;

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