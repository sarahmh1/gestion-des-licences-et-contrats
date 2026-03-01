package com.example.projet2024.service;

import com.example.projet2024.entite.wallix.Wallix;

import java.util.List;

public interface IWallixService {
    Wallix addWallix(Wallix wallix);
    Wallix updateWallix(Wallix wallix);
    Wallix retrieveWallix(Long wallixId);
    List<Wallix> retrieveAllWallixs();
    void deleteById(Long wallixId);
    void activate(Long id);
    Wallix updateWallixFile(Long wallixId, String fichier, String fichierOriginalName);
}
