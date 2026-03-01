package com.example.projet2024.service;

import com.example.projet2024.entite.Varonis;

import java.util.List;

public interface IVaronisService {
    Varonis addVaronis(Varonis varonis);
    Varonis updateVaronis(Varonis varonis) ;
    Varonis retrieveVaronis(Long varonisid);
    List<Varonis> retrieveAllVaroniss();
    void deleteById (Long varonisid);
    void activate (Long id ) ;
    void updateVaronisFile(Long id, String fichier, String fichierOriginalName);
}
