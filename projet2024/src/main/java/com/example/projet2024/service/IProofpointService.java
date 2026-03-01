package com.example.projet2024.service;

import com.example.projet2024.entite.proofpoint.Proofpoint;

import java.util.List;

public interface IProofpointService {
    Proofpoint addProofpoint(Proofpoint proofpoint);
    Proofpoint updateProofpoint(Proofpoint proofpoint);
    Proofpoint retrieveProofpoint(Long proofpointId);
    List<Proofpoint> retrieveAllProofpoints();
    void deleteById(Long proofpointId);
    void activate(Long id);
    Proofpoint updateProofpointFile(Long proofpointId, String fichier, String fichierOriginalName);
}
