package it.epicode.Capstone.databasePucSassari.sezioni.usipermessi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsiPermessiResponse {
    private Long id;
    private String zona;
    private String macrocategorie;
    private String descrizione;
    private List<String> usi;
    private String note;
}