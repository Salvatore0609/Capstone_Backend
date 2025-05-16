package it.epicode.Capstone.login.utenti.MyProject.stepdata;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepDataRequest {
    @NotNull
    private Long Id;
    @NotNull
    private Long projectId;

    @NotNull
    private Long faseId;

    @NotNull
    private Long taskId;

    @NotNull
    private Long stepId;

    private String textareaValue;
    private String dropdownSelected;
    private Boolean checkboxValue;
}