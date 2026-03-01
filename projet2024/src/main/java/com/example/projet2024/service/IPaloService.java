package com.example.projet2024.service;

import com.example.projet2024.entite.paloalto.Palo;

import java.util.List;

public interface IPaloService {
    Palo addPalo(Palo palo);
    Palo updatePalo(Palo palo);
    Palo updatePaloFile(Long paloId, String fichier, String fichierOriginalName);
    Palo retrievePalo(Long paloId);
    List<Palo> retrieveAllPalos();
    void deleteById(Long paloId);
    void activate(Long id);
}
