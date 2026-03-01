package com.example.projet2024.Enum;

public enum StatutInterventionPreventive {
    CREE,                      // Créé par l'administrateur, en attente de complétion
    EN_ATTENTE_INTERVENTION,   // Admin a complété, en attente du technicien
    TERMINE                    // Technicien a complété, intervention terminée
}
