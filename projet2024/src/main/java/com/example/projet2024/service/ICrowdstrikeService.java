package com.example.projet2024.service;

import com.example.projet2024.entite.Crowdstrike;


import java.util.List;

public interface ICrowdstrikeService {
    Crowdstrike addCrowdstrike(Crowdstrike crowdstrike);
    Crowdstrike updateCrowdstrike(Crowdstrike crowdstrike);
    Crowdstrike retrieveCrowdstrike(Long crowdstrikeId);
    List<Crowdstrike> retrieveAllCrowdstrikes();
    void deleteById(Long crowdstrikeId);
    void activate(Long id);
    Crowdstrike updateCrowdstrikeFile(Long crowdstrikeId, String fichier, String fichierOriginalName);
}
