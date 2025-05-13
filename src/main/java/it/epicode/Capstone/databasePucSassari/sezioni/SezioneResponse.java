package it.epicode.Capstone.databasePucSassari.sezioni;

import it.epicode.Capstone.databasePucSassari.sezioni.usipermessi.UsiPermessiResponse;
import it.epicode.Capstone.databasePucSassari.sottozone.SottozoneResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SezioneResponse {
    private Long sezId;  // Id della sezione
    private String titolo;  // Titolo della sezione
    private List<String> contenuto;  // Contenuti della sezione
    private List<SottozoneResponse> sottozone;  // Lista delle sottozone
    private List<String> categorie;  // Lista delle categorie
    private List<UsiPermessiResponse> usiPermessi;  // Lista degli usi permessi
    private List<String> parametriUrbanistici;  // Parametri urbanistici

}