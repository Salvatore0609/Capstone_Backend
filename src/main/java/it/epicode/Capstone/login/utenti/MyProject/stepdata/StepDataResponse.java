package it.epicode.Capstone.login.utenti.MyProject.stepdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepDataResponse {
    private Long id;

    private Long projectId;
    private Long faseId;
    private Long taskId;
    private Long stepId;
    private String fileName;
    private String fileType;
    private Integer fileSize;
    private String fileUrl;
    private String textareaValue;
    private String dropdownSelected;
    private Boolean checkboxValue;
    private LocalDateTime updatedAt;
}