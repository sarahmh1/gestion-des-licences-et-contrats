package com.example.projet2024.service;

import com.example.projet2024.entite.veeam.Veeam;

import java.util.List;

public interface IVeeamService {
    Veeam addVeeam(Veeam veeam);
    Veeam updateVeeam(Veeam veeam);
    Veeam retrieveVeeam(Long veeamId);
    List<Veeam> retrieveAllVeeams();
    void deleteById(Long veeamId);
    void activate(Long id);
    Veeam updateVeeamFile(Long veeamId, String fichier, String fichierOriginalName);
}
