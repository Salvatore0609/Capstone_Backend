package it.epicode.Capstone.login.utenti.MyProject.stepdata;

import it.epicode.Capstone.login.authGoogle.UtenteGoogle;
import it.epicode.Capstone.login.utenti.Utente;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/project/step-data")
@RequiredArgsConstructor
public class StepDataController {

    private final StepDataService stepDataService;

    @PostMapping("/upload")
    public ResponseEntity<StepDataResponse> uploadFileStepData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @RequestParam("faseId") Long faseId,
            @RequestParam("taskId") Long taskId,
            @RequestParam("stepId") Long stepId,
            @AuthenticationPrincipal Object user
    ) {
        StepDataResponse saved = stepDataService.saveFileStepData(file, projectId, faseId, taskId, stepId, user);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<StepDataResponse>> getStepDataForProject(@RequestParam Long projectId) {
        List<StepDataResponse> dataList = stepDataService.listByProject(projectId);
        return ResponseEntity.ok(dataList);
    }

    @GetMapping("/my-steps")
    public ResponseEntity<List<StepDataResponse>> getMyStepData(
            @AuthenticationPrincipal Object user) {

        if (user instanceof UtenteGoogle googleUser) {
            return ResponseEntity.ok(
                    stepDataService.getStepDataByGoogleUser(googleUser.getId())
            );
        }
        else if (user instanceof Utente tradUser) {
            return ResponseEntity.ok(
                    stepDataService.getStepDataByUser(tradUser.getId())
            );
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    public ResponseEntity<StepDataResponse> saveStepData(
            @RequestBody StepDataRequest request,
            @AuthenticationPrincipal Object user
    ) {
        StepDataResponse saved = stepDataService.saveOrUpdateStepData(request, user);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStepData(@PathVariable Long id) {
        stepDataService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
