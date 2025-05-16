package it.epicode.Capstone.login.utenti.MyProject;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByProprietarioId(Long proprietarioId);
    List<Project> findByProprietarioGoogleId(Long proprietarioGoogleId);

}