package it.epicode.Capstone.login.utenti.MyProject.stepdata;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StepDataRepository extends JpaRepository<StepData, Long> {
    List<StepData> findByProgettoId(Long projectId);
    List<StepData> findByProprietarioGoogleId(Long proprietarioGoogleId);
}