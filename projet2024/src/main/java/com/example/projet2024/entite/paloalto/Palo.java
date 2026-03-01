package com.example.projet2024.entite.paloalto;
import com.example.projet2024.entite.CommandePasserPar;
import com.example.projet2024.entite.Duree;


import com.example.projet2024.entite.LicenceFortinet;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Palo")
public class Palo {
    @Id
    @Column(name = "PaloId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paloId;

    @Basic
    @Column(name = "Client")
    private String client;

    @Basic
    @Column(name = "Nom_Du_Boitier")
    private String nomDuBoitier;


    @Basic
    @Column(name = "Numero_Serie_Boitier")
    private String numeroSerieBoitier;






    @Enumerated(EnumType.STRING)
    @Column(name = "Duree_De_Licence")
    private Duree dureeDeLicence;

    @Basic
    @Column(name = "Nom_Du_Contact")
    private String nomDuContact;

    @Basic
    @Column(name = "Adresse_Email_Contact")
    private String adresseEmailContact;
    @ElementCollection(fetch = FetchType.EAGER)
    @JsonProperty("ccMail") // correspond au nom JSON dans Postman
    private List<String> ccMail  ;

    @Basic
    @Column(name = "Adresse_mail_admin")
    private String mailAdmin  ;
    @Basic
    @Column(name = "Numero")
    private String numero;

    @Basic
    @Column(name = "Sous_Contrat")
    private Boolean sousContrat;

    @Basic
    @Column(name = "Remarque")
    private String remarque;
    private boolean approuve ;
    @Enumerated(EnumType.STRING)
    @Column(name = "Commande_Passer_Par")
    private CommandePasserPar commandePasserPar ;

    @OneToMany(mappedBy = "palo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("palo-licence")
    private List<LicenceFortinet> licences;
    @Column(nullable = false) // Empêche d'accepter des valeurs null
    private boolean emailSent6Months = false;



    @Column(nullable = false)
    private boolean emailSent5Months = false;

    @Column(nullable = false)
    private boolean emailSent4Months = false;

    @Column(nullable = false)
    private boolean emailSent3Months = false;

    @Column(nullable = false)
    private boolean emailSent2Months = false;

    @Column(nullable = false)
    private boolean emailSent1Month = false;

    @Column(nullable = false)
    private boolean emailSent7Weeks = false;

    @Column(nullable = false)
    private boolean emailSent6Weeks = false;

    @Column(nullable = false)
    private boolean emailSent5Weeks = false;

    @Column(nullable = false)
    private boolean emailSent3Weeks = false;

    @Column(nullable = false)
    private boolean emailSent2Weeks = false;

    @Column(nullable = false)
    private boolean emailSent1Week = false;

    @Column(nullable = false)
    private boolean emailSentDayOf = false;

    @Basic
    @Column(name = "fichier")
    private String fichier;

    @Basic
    @Column(name = "fichier_original_name")
    private String fichierOriginalName;

}
