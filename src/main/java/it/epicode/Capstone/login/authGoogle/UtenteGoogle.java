package it.epicode.Capstone.login.authGoogle;

import it.epicode.Capstone.login.auth.Role;
import it.epicode.Capstone.login.utenti.MyProject.Project;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
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

    /*@OneToMany
    private List<Project> projects;*/

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
}
