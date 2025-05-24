package it.epicode.Capstone.login.utenti.MyProject.stepdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.epicode.Capstone.cloudinary.CloudinaryService;
import it.epicode.Capstone.databasePucSassari.articoli.*;
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
import java.util.Map;
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
    private final ArticoloService articoloService;

    //ObjectMapper per generare JSON leggibile (pretty‐print facoltativo)
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);


    private final CloudinaryService cloudinaryService;

    public StepDataResponse saveFileStepData(
            MultipartFile file,
            Long projectId,
            Long faseId,
            Long taskId,
            Long stepId,
            Object user) {

        // Ora uploadRawFile ritorna una Map<String, String>
        Map<String, String> uploadResult = cloudinaryService.uploadRawFile(file);

        String fileUrl = uploadResult.get("fileUrl");
        String fileNameFromCloud = uploadResult.get("fileName"); // potrebbe essere uguale a file.getOriginalFilename()

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

        stepData.setFileName(fileNameFromCloud);  // uso il nome dal risultato dell'upload
        stepData.setFileUrl(fileUrl);
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


    public List<StepDataResponse> getStepDataByGoogleUser(Long googleUserId) {
        return stepDataRepository.findByProgetto_ProprietarioGoogle_Id(googleUserId)
                .stream().map(this::toResponse).toList();
    }


    public List<StepDataResponse> getStepDataByUser(Long userId) {
        return stepDataRepository.findByProgetto_Proprietario_Id(userId)
                .stream().map(this::toResponse).toList();
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

        // ——— Nuova logica per “snapshottare” l’articolo esistente ———
        if (request.getArtId() != null) {
            // 1) Recupero il contenuto completo dell’articolo come DTO (ArticoloResponse)
            ArticoloResponse artResp = articoloService.findArticleById(request.getArtId())
                    .orElseThrow(() -> new RuntimeException("Articolo non trovato con ID: " + request.getArtId()));

            // 2) Serializzo quell’ArticoloResponse in JSON
            try {
                String jsonArticolo = objectMapper.writeValueAsString(artResp);
                stepData.setArticoloSnapshot(jsonArticolo);
            } catch (Exception e) {
                throw new RuntimeException("Errore durante la serializzazione dell'articolo", e);
            }

            // devo settare anche la FK sull’entità Articolo,
            // altrimenti il campo `stepData.getArticolo()` rimane null.
            Articolo artEntity = articoloService.getById(request.getArtId());
            stepData.setArticolo(artEntity);

        } else {
            // Nessun articolo selezionato: azzero snapshot e FK
            stepData.setArticoloSnapshot(null);
            stepData.setArticolo(null);
        }
        // ————————————————————————————————————————————————————————

        stepData.setTextareaValue(request.getTextareaValue());
        stepData.setDropdownSelected(request.getDropdownSelected());
        stepData.setCheckboxValue(request.getCheckboxValue());

        stepData.setFileName(request.getFileName());
        stepData.setFileUrl(request.getFileUrl());
        stepData.setFileType(request.getFileType());
        stepData.setFileSize(request.getFileSize());

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
        dto.setProjectId(stepData.getProgetto().getId());
        dto.setFaseId(stepData.getFase().getId());
        dto.setTaskId(stepData.getTask().getId());
        dto.setStepId(stepData.getStep().getId());

        if (stepData.getArticolo() != null) {
            dto.setArtId(stepData.getArticolo().getArtId());
        } else {
            dto.setArtId(null);
        }

        //popolare anche il JSON salvato:
        dto.setArticoloSnapshot(stepData.getArticoloSnapshot());

        dto.setFileName(stepData.getFileName());
        dto.setFileUrl(stepData.getFileUrl());
        dto.setFileType(stepData.getFileType());
        dto.setFileSize(stepData.getFileSize());
        dto.setTextareaValue(stepData.getTextareaValue());
        dto.setDropdownSelected(stepData.getDropdownSelected());
        dto.setCheckboxValue(stepData.getCheckboxValue());
        dto.setUpdatedAt(stepData.getUpdatedAt());
        return dto;
    }
}
