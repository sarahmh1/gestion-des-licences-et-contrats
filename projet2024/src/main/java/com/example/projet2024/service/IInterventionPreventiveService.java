package com.example.projet2024.service;

import com.example.projet2024.entite.InterventionPreventive;

import java.util.List;

public interface IInterventionPreventiveService {
    List<InterventionPreventive> getAllInterventionsPreventives();
    InterventionPreventive getInterventionPreventiveById(Long id);
    InterventionPreventive addInterventionPreventive(InterventionPreventive intervention);
    InterventionPreventive updateInterventionPreventive(Long id, InterventionPreventive intervention);
    void deleteInterventionPreventive(Long id);
    List<InterventionPreventive> searchInterventionsPreventives(String searchTerm);
    List<InterventionPreventive> getByContratId(Long contratId);
    void updateInterventionPreventiveFile(Long id, String fichier, String fichierOriginalName);
}
