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
    private String fileName;
    private String fileType;
    private Integer fileSize;
    private String textareaValue;
    private String dropdownSelected;
    private Boolean checkboxValue;
    private LocalDateTime updatedAt;
}