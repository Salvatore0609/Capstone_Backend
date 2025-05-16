package it.epicode.Capstone.login.utenti.MyProject.stepdata;

import it.epicode.Capstone.cloudinary.CloudinaryService;
import it.epicode.Capstone.login.authGoogle.UtenteGoogle;
import it.epicode.Capstone.login.authGoogle.UtenteGoogleRepository;
import it.epicode.Capstone.login.utenti.MyProject.ProjectRepository;
import it.epicode.Capstone.login.utenti.MyProject.fasi.FaseRepository;
import it.epicode.Capstone.login.utenti.MyProject.fasi.tasks.TaskRepository;
import it.epicode.Capstone.login.utenti.MyProject.fasi.tasks.steps.StepRepository;
import it.epicode.Capstone.login.utenti.Utente;
import it.epicode.Capstone.login.utenti.UtenteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StepDataService {

    private final StepDataRepository stepDataRepository;
    private final ProjectRepository projectRepository;
    private final FaseRepository faseRepository;
    private final TaskRepository taskRepository;
    private final StepRepository stepRepository;

    private final UtenteRepository utenteRepository;
    private final UtenteGoogleRepository utenteGoogleRepository;

    private final CloudinaryService cloudinaryService;

    public StepDataResponse saveFileStepData(
            MultipartFile file,
            Long projectId,
            Long faseId,
            Long taskId,
            Long stepId,
            Object user) {

        String url = cloudinaryService.uploadImage(file);

        StepData stepData = new StepData();
        stepData.setProgetto(projectRepository.findById(projectId).orElseThrow());
        stepData.setFase(faseRepository.findById(faseId).orElseThrow());
        stepData.setTask(taskRepository.findById(taskId).orElseThrow());
        stepData.setStep(stepRepository.findById(stepId).orElseThrow());

        if (user instanceof Utente utente) {
            stepData.setUtente(utente);
        } else if (user instanceof UtenteGoogle utenteGoogle) {
            stepData.setUtenteGoogle(utenteGoogle);
        }

        stepData.setFileName(url);
        stepData.setFileType(file.getContentType());
        stepData.setFileSize((int) file.getSize());
        stepData.setUpdatedAt(LocalDateTime.now());

        StepData saved = stepDataRepository.save(stepData);
        return toResponse(saved);
    }

    public List<StepDataResponse> listByProject(Long projectId) {
        return stepDataRepository.findByProgettoId(projectId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public StepDataResponse saveOrUpdateStepData(StepDataRequest request, Object user) {
        StepData stepData;

        if (request.getId() != null) {
            stepData = stepDataRepository.findById(request.getId())
                    .orElse(new StepData());
        } else {
            stepData = new StepData();
        }

        stepData.setProgetto(projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found")));
        stepData.setFase(faseRepository.findById(request.getFaseId())
                .orElseThrow(() -> new RuntimeException("Fase not found")));
        stepData.setTask(taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found")));
        stepData.setStep(stepRepository.findById(request.getStepId())
                .orElseThrow(() -> new RuntimeException("Step not found")));

        if (user instanceof Utente utente) {
            stepData.setUtente(utente);
            stepData.setUtenteGoogle(null);
        } else if (user instanceof UtenteGoogle utenteGoogle) {
            stepData.setUtenteGoogle(utenteGoogle);
            stepData.setUtente(null);
        }

        stepData.setTextareaValue(request.getTextareaValue());
        stepData.setDropdownSelected(request.getDropdownSelected());
        stepData.setCheckboxValue(request.getCheckboxValue());
        stepData.setUpdatedAt(LocalDateTime.now());

        StepData saved = stepDataRepository.save(stepData);
        return toResponse(saved);
    }
    public void deleteById(Long id) {
        stepDataRepository.deleteById(id);
    }

    private StepDataResponse toResponse(StepData stepData) {
        StepDataResponse dto = new StepDataResponse();
        dto.setId(stepData.getId());
        dto.setFileName(stepData.getFileName());
        dto.setFileType(stepData.getFileType());
        dto.setFileSize(stepData.getFileSize());
        dto.setTextareaValue(stepData.getTextareaValue());
        dto.setDropdownSelected(stepData.getDropdownSelected());
        dto.setCheckboxValue(stepData.getCheckboxValue());
        dto.setUpdatedAt(stepData.getUpdatedAt());
        return dto;
    }
}
