package com.example.projet2024.service;

import com.example.projet2024.entite.InterventionCurative;
import com.example.projet2024.entite.Intervenant;
import com.example.projet2024.repository.InterventionCurativeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class InterventionCurativeServiceImpl implements IInterventionCurativeService {

    @Autowired
    private InterventionCurativeRepository interventionCurativeRepository;

    @Override
    public List<InterventionCurative> getAllInterventionsCuratives() {
        return interventionCurativeRepository.findAll();
    }

    @Override
    public InterventionCurative getInterventionCurativeById(Long id) {
        return interventionCurativeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention Curative non trouvée avec l'id: " + id));
    }

    @Override
    public InterventionCurative addInterventionCurative(InterventionCurative intervention) {
        // Associer les intervenants à l'intervention
        if (intervention.getIntervenants() != null) {
            for (Intervenant intervenant : intervention.getIntervenants()) {
                intervenant.setInterventionCurative(intervention);
            }
        }
        return interventionCurativeRepository.save(intervention);
    }

    @Override
    public InterventionCurative updateInterventionCurative(Long id, InterventionCurative intervention) {
        InterventionCurative existing = getInterventionCurativeById(id);
        
        existing.setFicheIntervention(intervention.getFicheIntervention());
        existing.setNomClient(intervention.getNomClient());
        existing.setCriticite(intervention.getCriticite());
        existing.setIntervenant(intervention.getIntervenant());
        existing.setDateHeureDemande(intervention.getDateHeureDemande());
        existing.setDateHeureIntervention(intervention.getDateHeureIntervention());
        existing.setDateHeureResolution(intervention.getDateHeureResolution());
        existing.setDureeIntervention(intervention.getDureeIntervention());
        existing.setModeIntervention(intervention.getModeIntervention());
        existing.setVisAVisClient(intervention.getVisAVisClient());
        existing.setEnCoursDeResolution(intervention.getEnCoursDeResolution());
        existing.setResolu(intervention.getResolu());
        existing.setTachesEffectuees(intervention.getTachesEffectuees());
        existing.setContrat(intervention.getContrat());
        
        // Mettre à jour les intervenants
        existing.getIntervenants().clear();
        if (intervention.getIntervenants() != null) {
            for (Intervenant intervenant : intervention.getIntervenants()) {
                intervenant.setInterventionCurative(existing);
                existing.getIntervenants().add(intervenant);
            }
        }
        
        return interventionCurativeRepository.save(existing);
    }

    @Override
    public void deleteInterventionCurative(Long id) {
        interventionCurativeRepository.deleteById(id);
    }

    @Override
    public List<InterventionCurative> searchInterventionsCuratives(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllInterventionsCuratives();
        }
        
        List<InterventionCurative> byClient = interventionCurativeRepository.findByNomClientContainingIgnoreCase(searchTerm);
        List<InterventionCurative> byIntervenant = interventionCurativeRepository.findByIntervenantContainingIgnoreCase(searchTerm);
        
        return Stream.concat(byClient.stream(), byIntervenant.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<InterventionCurative> getByContratId(Long contratId) {
        return interventionCurativeRepository.findByContratContratId(contratId);
    }

    @Override
    public void updateInterventionCurativeFile(Long id, String fichier, String fichierOriginalName) {
        InterventionCurative intervention = getInterventionCurativeById(id);
        intervention.setFichier(fichier);
        intervention.setFichierOriginalName(fichierOriginalName);
        interventionCurativeRepository.save(intervention);
    }
}
