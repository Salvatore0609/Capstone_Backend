package it.epicode.Capstone.databasePucSassari.sottozone.parametri;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParametriRequest {
     private String indiceFondiario;
     private String altezzaMassima;
     private String rapportoCopertura;
     private String intensitaTerritorialeMassima;
     private String lottoMinimo;
     private String volumeMassimo;
     private String indiceTerritoriale;
     private List<String> volumetria;
     private String incremento;
     private String deroga;
     private String tipo;
     private String indice;
     private String note;
}