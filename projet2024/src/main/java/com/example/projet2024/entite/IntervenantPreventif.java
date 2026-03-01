package com.example.projet2024.entite;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Intervenant_Preventif")
public class IntervenantPreventif {
    @Id
    @Column(name = "IntervenantPreventifId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long intervenantPreventifId;

    @Basic
    @Column(name = "Nom")
    private String nom;

    @ManyToOne
    @JoinColumn(name = "InterventionPreventiveId")
    @JsonBackReference
    private InterventionPreventive interventionPreventive;

    // Relation vers PeriodeLigne (requis par PeriodeLigne.intervenants mappedBy)
    @ManyToOne
    @JoinColumn(name = "PeriodeLigneId")
    @JsonBackReference("intervenants")
    private PeriodeLigne periodeLigne;
}
