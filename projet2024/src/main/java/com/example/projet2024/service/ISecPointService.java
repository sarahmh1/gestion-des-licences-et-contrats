package com.example.projet2024.service;

import com.example.projet2024.entite.secpoint.SecPoint;

import java.util.List;

public interface ISecPointService {
    SecPoint addSecPoint(SecPoint secPoint);
    SecPoint updateSecPoint(SecPoint secPoint);
    SecPoint retrieveSecPoint(Long secPointId);
    List<SecPoint> retrieveAllSecPoints();
    void deleteById(Long secPointId);
    void activate(Long id);
    SecPoint updateSecPointFile(Long secPointId, String fichier, String fichierOriginalName);
}
