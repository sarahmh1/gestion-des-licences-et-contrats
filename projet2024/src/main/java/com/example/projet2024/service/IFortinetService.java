package com.example.projet2024.service;

import com.example.projet2024.entite.fortinet.Fortinet;

import java.util.List;

public interface IFortinetService {
    Fortinet addFortinet(Fortinet fortinet);
    Fortinet updateFortinet(Fortinet fortinet);
    Fortinet updateFortinetFile(Long fortinetId, String fichier, String fichierOriginalName);
    Fortinet retrieveFortinet(Long fortinetId);
    List<Fortinet> retrieveAllFortinets();
    void deleteById(Long fortinetId);
    void activate(Long id);
}
