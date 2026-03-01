package com.example.projet2024.service;

import com.example.projet2024.entite.Contrat;

import java.util.List;

public interface IContratService {
    List<Contrat> getAllContrats();
    Contrat getContratById(Long id);
    Contrat addContrat(Contrat contrat);
    Contrat updateContrat(Long id, Contrat contrat);
    void deleteContrat(Long id);
    List<Contrat> searchContrats(String searchTerm);
    void updateContratFile(Long id, String fichier, String fichierOriginalName);
}
