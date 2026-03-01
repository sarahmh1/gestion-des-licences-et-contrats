package com.example.projet2024.service;

import com.example.projet2024.entite.F5;
import com.example.projet2024.entite.Fortra;

import java.util.List;

public interface IFortraService {
    Fortra addFortra(Fortra fortra);
    Fortra updateFortra(Fortra fortra) ;
    Fortra updateFortraFile(Fortra fortra);
    Fortra retrieveFortra(Long fortraid);
    List<Fortra> retrieveAllFortras();
    void deleteById (Long fortraid);
    void activate (Long id ) ;
}
