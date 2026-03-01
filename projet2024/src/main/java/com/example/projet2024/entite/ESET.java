package com.example.projet2024.entite;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "ESET")
public class ESET {
    @Id
    @Column(name = "esetid")
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long esetid ;
    @Basic
    @Column(name = "Client")
    private String client ;
    @Basic
    @Column(name = "Identifiant")
    private String identifiant ;
    //
    @Basic
    @Column(name = "Cle_de_Licence")
    private String cle_de_Licence  ;
    //
    @Enumerated(EnumType.STRING)
    @Column(name = "Nom_produit")
    private NomProduit nom_produit ;
    //
    //
    @Basic
    @Column(name = "nmb_de_Licence")
    private int nombre  ;
    //
    //
    @Basic
    @Column(name = "nmb_tlf")
    private int nmb_tlf  ;
    //
    //
    @Basic
    @Column(name = "Nom_contact")
    private  String nom_contact ;
    //
    //
    @Enumerated(EnumType.STRING)
    @Column(name = "Commande_Passer_Par")
    private CommandePasserPar commandePasserPar ;
    @ElementCollection(fetch = FetchType.EAGER)
    @JsonProperty("ccMail") // correspond au nom JSON dans Postman
    private List<String> ccMail  ;
    //
    @Basic
    @Column(name = "Adresse_mail_admin")
    private String mailAdmin  ;
    @Basic
    @Column(name = "mail")
    private String mail ;
    @Basic
    @Column(name = "Sous_Contrat")
    private Boolean sousContrat;

    @Basic
    @Column(name = "Remarque")
    private String remarque;
    @Basic
    @Column(name = "Duree_De_Licence")
    private Duree dureeDeLicence;
    @Basic
    @Column(name = "dateEx")
//    @Temporal (TemporalType.DATE)
    private LocalDate dateEx  ;

    @Enumerated(EnumType.STRING)
    @Column(name = "TypeAchat")
    private TypeAchat typeAchat ;



    private boolean approuve ;

    @Basic
    @Column(name = "fichier")
    private String fichier;

    @Basic
    @Column(name = "fichier_original_name")
    private String fichierOriginalName;

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




}
