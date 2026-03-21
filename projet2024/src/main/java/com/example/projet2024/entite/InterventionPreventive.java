package com.example.projet2024.entite;

import com.example.projet2024.Enum.StatutInterventionPreventive;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.example.projet2024.entite.PeriodeLigne;

@Getter
@Setter
@Entity
@Table(name = "Intervention_Preventive")
public class InterventionPreventive {
    @Id
    @Column(name = "InterventionPreventiveId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interventionPreventiveId;

    @Basic
    @Column(name = "Nom_Client")
    private String nomClient;

    @Basic
    @Column(name = "Nb_Interventions_Par_An")
    private Integer nbInterventionsParAn;

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

    @Basic
    @Column(name = "Date_Intervention")
    private LocalDate dateIntervention;

    @Basic
    @Column(name = "Date_Rapport_Preventive")
    private LocalDate dateRapportPreventive;

    @OneToMany(mappedBy = "interventionPreventive", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<IntervenantPreventif> intervenants = new ArrayList<>();

    @OneToMany(mappedBy = "interventionPreventive", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("periodeLignes")
    private List<PeriodeLigne> periodeLignes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "ContratId")
    private Contrat contrat;

    @Basic
    @Column(name = "Fichier")
    private String fichier;

    @Basic
    @Column(name = "Fichier_Original_Name")
    private String fichierOriginalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "Statut", length = 50, columnDefinition = "VARCHAR(50)")
    private StatutInterventionPreventive statut = StatutInterventionPreventive.CREE;

    @Basic
    @Column(name = "Email_Commercial")
    private String emailCommercial;

    @ElementCollection
    @CollectionTable(name = "intervention_preventive_cc_mail", joinColumns = @JoinColumn(name = "intervention_preventive_id"))
    @Column(name = "cc_mail")
    private List<String> ccMail = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "intervention_preventive_assigned_users",
        joinColumns = @JoinColumn(name = "intervention_preventive_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> assignedUsers = new ArrayList<>();

    // Flags pour les notifications email
    @Basic
    @Column(name = "Email_Sent_1_Week_Before")
    private Boolean emailSent1WeekBefore = false;

    @Basic
    @Column(name = "Email_Sent_1_Month_Before")
    private Boolean emailSent1MonthBefore = false;

    @Basic
    @Column(name = "Email_Sent_Day_Of")
    private Boolean emailSentDayOf = false;

    @Column(name = "nom_produit", length = 100)
    private String nomProduit;
}
