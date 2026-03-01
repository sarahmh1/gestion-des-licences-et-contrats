package com.example.projet2024.service;

import com.example.projet2024.entite.Imperva;

import java.util.List;

public interface IImpervaService {
    Imperva addImperva(Imperva imperva);
    Imperva updateImperva(Imperva imperva) ;
    Imperva retrieveImperva(Long impervaid);
    List<Imperva> retrieveAllImpervas();
    void deleteById (Long impervaid);
    void activate (Long id ) ;
    void updateImpervaFile(Long id, String fichier, String fichierOriginalName);
}
