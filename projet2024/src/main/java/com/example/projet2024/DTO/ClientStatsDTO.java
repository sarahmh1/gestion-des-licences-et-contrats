package com.example.projet2024.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientStatsDTO {
    private String client;
    private String nomProduit;
    private double totalInterventionsCuratives;  // from contrat nbInterventionsCuratives (J/H)
    private double interventionsConsommees;       // sum of dureeIntervention from InterventionCurative
    private double interventionsNonConsommees;    // total - consommees
    private int totalInterventionsPreventives;    // from contrat nbInterventionsPreventives (2 or 4)
    private int preventivesConsommees;            // count of PeriodeLignes with technician info
    private int preventivesNonConsommees;         // total - consommees
}
