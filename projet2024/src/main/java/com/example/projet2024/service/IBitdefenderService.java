package com.example.projet2024.service;

import com.example.projet2024.entite.Bitdefender;

import java.util.List;

public interface IBitdefenderService {
    Bitdefender addBitdefender(Bitdefender bitdefender);
    Bitdefender updateBitdefender(Bitdefender bitdefender) ;
    Bitdefender retrieveBitdefender(Long bitdefenderid);
    List<Bitdefender> retrieveAllBitdefenders();
    void deleteById (Long bitdefenderid);
    void activate (Long id ) ;
    Bitdefender updateBitdefenderFile(Long bitdefenderId, String fichier, String fichierOriginalName);
}
