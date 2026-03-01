package com.example.projet2024.service;
import com.example.projet2024.entite.Infoblox;

import java.util.List;

public interface IInfobloxService {
    Infoblox addInfoblox(Infoblox infoblox);
    Infoblox updateInfoblox(Infoblox infoblox) ;
    Infoblox retrieveInfoblox(Long infobloxid);
    List<Infoblox> retrieveAllInfobloxs();
    void deleteById (Long infobloxid);
    void activate(Long id ) ;
    void updateInfobloxFile(Long id, String fichier, String fichierOriginalName);
}
