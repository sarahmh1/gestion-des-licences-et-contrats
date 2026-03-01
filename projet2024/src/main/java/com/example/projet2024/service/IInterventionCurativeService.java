package com.example.projet2024.service;

import com.example.projet2024.entite.InterventionCurative;

import java.util.List;

public interface IInterventionCurativeService {
    List<InterventionCurative> getAllInterventionsCuratives();
    InterventionCurative getInterventionCurativeById(Long id);
    InterventionCurative addInterventionCurative(InterventionCurative intervention);
    InterventionCurative updateInterventionCurative(Long id, InterventionCurative intervention);
    void deleteInterventionCurative(Long id);
    List<InterventionCurative> searchInterventionsCuratives(String searchTerm);
    List<InterventionCurative> getByContratId(Long contratId);
    void updateInterventionCurativeFile(Long id, String fichier, String fichierOriginalName);
}
