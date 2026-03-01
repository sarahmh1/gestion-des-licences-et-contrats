package com.example.projet2024.entite;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ESETNFR")
public class ESETNFR {

    @Id
    @Column(name = "esetid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Basic
    @Column(name = "Adresse_mail")
    private String mail  ;
    //
    @Basic
    @Column(name = "Adresse_mail_admin")
    private String mailAdmin  ;

    @Basic
    @Column(name = "dateEx")
    @Temporal (TemporalType.DATE)
    private LocalDate dateEx  ;

    @Enumerated(EnumType.STRING)
    @Column(name = "TypeAchat")
    private TypeAchat typeAchat ;



    private boolean approuve ;
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


    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> concernedPersonsEmails;

}
