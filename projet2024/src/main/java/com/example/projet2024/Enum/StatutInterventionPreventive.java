package com.example.projet2024.Enum;

public enum StatutInterventionPreventive {
    CREE,                      // Créé par l'administrateur, en attente de complétion
    EN_ATTENTE_INTERVENTION,   // Admin a complété, en attente du technicien
    EN_COURS,                  // Technicien a commencé mais pas terminé toutes les lignes
    TERMINE                    // Technicien a complété toutes les lignes de période
}
