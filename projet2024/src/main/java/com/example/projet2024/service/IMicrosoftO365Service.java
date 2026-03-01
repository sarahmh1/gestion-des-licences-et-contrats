package com.example.projet2024.service;

import com.example.projet2024.entite.microsofto365.MicrosoftO365;

import java.util.List;

public interface IMicrosoftO365Service {
    MicrosoftO365 addMicrosoftO365(MicrosoftO365 microsoftO365);
    MicrosoftO365 updateMicrosoftO365(MicrosoftO365 microsoftO365);
    MicrosoftO365 retrieveMicrosoftO365(Long microsoftO365Id);
    List<MicrosoftO365> retrieveAllMicrosoftO365s();
    void deleteById(Long microsoftO365Id);
    void activate(Long id);
    void updateMicrosoftO365File(Long id, String fichier, String fichierOriginalName);
}
