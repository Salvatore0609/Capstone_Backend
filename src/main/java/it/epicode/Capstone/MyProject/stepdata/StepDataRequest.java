package it.epicode.Capstone.MyProject.stepdata;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepDataRequest {

    private Long id;
    @NotNull
    private Long projectId;

    @NotNull
    private Long faseId;

    @NotNull
    private Long taskId;

    @NotNull
    private Long stepId;

    private Long artId;

    private String fileName;
    private String fileType;
    private Integer fileSize;
    private String fileUrl;
    private String textareaValue;
    private String dropdownSelected;
    private Boolean checkboxValue;
}