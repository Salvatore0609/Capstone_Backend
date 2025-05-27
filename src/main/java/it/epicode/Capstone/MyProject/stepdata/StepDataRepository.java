package it.epicode.Capstone.MyProject.stepdata;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StepDataRepository extends JpaRepository<StepData, Long> {
    List<StepData> findByProgettoId(Long projectId);

    // Cerca tramite il proprietario del progetto (Utente tradizionale)
    List<StepData> findByProgetto_Proprietario_Id(Long proprietarioId);

    // Cerca tramite il proprietario Google del progetto
    List<StepData> findByProgetto_ProprietarioGoogle_Id(Long proprietarioGoogleId);
}