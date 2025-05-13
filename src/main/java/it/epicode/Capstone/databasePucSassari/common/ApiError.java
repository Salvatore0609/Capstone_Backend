package it.epicode.Capstone.databasePucSassari.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private String message;
    private int status;
    private Map<String, String> details; // Dettagli degli errori specifici
}
