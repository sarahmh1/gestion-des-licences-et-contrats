package com.example.projet2024.entite.rapid7;

import com.example.projet2024.entite.CommandePasserPar;
import com.example.projet2024.entite.LicenceFortinet;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Rapid7")
public class Rapid7 {
    @Id
    @Column(name = "Rapid7Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rapid7Id;

    @Basic
    @Column(name = "Client")
    private String client;
    @OneToMany(mappedBy = "rapid7", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("rapid7-licence")
    private List<LicenceFortinet> licences;

    @Basic
    @Column(name = "Cle_Licences")
    private String cleLicences;

    @Enumerated(EnumType.STRING)
    @Column(name = "Commande_Passer_Par")
    private CommandePasserPar commandePasserPar ;
    @Basic
    @Column(name = "Duree_De_Licence")
    private String dureeDeLicence;

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

    @Column(name = "fichier")
    private String fichier;

    @Column(name = "fichier_original_name")
    private String fichierOriginalName;

}



