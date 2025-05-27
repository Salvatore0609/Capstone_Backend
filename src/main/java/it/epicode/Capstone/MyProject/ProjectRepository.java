package it.epicode.Capstone.MyProject;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByProprietarioIdOrderByCreatedAtDesc(Long proprietarioId);
    List<Project> findByProprietarioGoogleIdOrderByCreatedAtDesc(Long proprietarioGoogleId);

    List<Project> findByProprietarioIdAndCompletatoTrue(Long proprietarioId);
    List<Project> findByProprietarioGoogleIdAndCompletatoTrue(Long proprietarioGoogleId);
}