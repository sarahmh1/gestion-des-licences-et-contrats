package com.example.projet2024.service;

import com.example.projet2024.entite.Cisco;

import java.util.List;

public interface ICiscoService {
    Cisco addCisco(Cisco cisco);
    Cisco updateCisco(Cisco cisco) ;
    Cisco retrieveCisco(Long ciscoid);
    List<Cisco> retrieveAllCiscos();
    void deleteById (Long ciscoid);
    void activate (Long id ) ;
    Cisco updateCiscoFile(Long id, String fichier, String fichierOriginalName);
}
