package com.example.projet2024.entite;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Periode_Ligne")
public class PeriodeLigne {
    @Id
    @Column(name = "PeriodeLigneId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long periodeLigneId;

    @Basic
    @Column(name = "Periode_De")
    private LocalDate periodeDe;

    @Basic
    @Column(name = "Periode_A")
    private LocalDate periodeA;

    @Basic
    @Column(name = "Periode_Recommandee_De")
    private LocalDate periodeRecommandeDe;

    @Basic
    @Column(name = "Periode_Recommandee_A")
    private LocalDate periodeRecommandeA;

    @Basic
    @Column(name = "Date_Intervention_Exigee")
    private LocalDate dateInterventionExigee;

    // Champs techniques (par ligne)
    @Basic
    @Column(name = "Date_Intervention")
    private LocalDate dateIntervention;

    @Basic
    @Column(name = "Date_Rapport_Preventive")
    private LocalDate dateRapportPreventive;

    @Basic
    @Column(name = "Fichier")
    private String fichier;

    @Basic
    @Column(name = "Fichier_Original_Name")
    private String fichierOriginalName;

    @Basic
    @Column(name = "Remarque", columnDefinition = "TEXT")
    private String remarque;

    @OneToMany(mappedBy = "periodeLigne", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("intervenants")
    private List<IntervenantPreventif> intervenants = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "InterventionPreventiveId")
    @JsonBackReference("periodeLignes")
    private InterventionPreventive interventionPreventive;

    // Flags pour les notifications email - Période Recommandée
    @Basic
    @Column(name = "Email_Sent_1_Week_Before")
    private Boolean emailSent1WeekBefore = false;

    @Basic
    @Column(name = "Email_Sent_1_Month_Before")
    private Boolean emailSent1MonthBefore = false;

    @Basic
    @Column(name = "Email_Sent_Day_Of")
    private Boolean emailSentDayOf = false;

    // Flags pour les notifications email - Période (contrat)
    @Basic
    @Column(name = "Email_Sent_Periode_1_Week_Before")
    private Boolean emailSentPeriode1WeekBefore = false;

    @Basic
    @Column(name = "Email_Sent_Periode_Day_Of")
    private Boolean emailSentPeriodeDayOf = false;
}
