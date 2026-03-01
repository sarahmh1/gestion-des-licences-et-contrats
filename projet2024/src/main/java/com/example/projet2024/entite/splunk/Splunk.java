package com.example.projet2024.entite.splunk;

import com.example.projet2024.entite.CommandePasserPar;
import com.example.projet2024.entite.Duree;
import com.example.projet2024.entite.LicenceFortinet;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "Splunk")
public class Splunk {
    @Id
    @Column(name = "Splunkid")
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long Splunkid ;
    @Basic
    @Column(name = "Client")
    private String client ;
    @OneToMany(mappedBy = "splunk", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("splunk-licence")
    private List<LicenceFortinet> licences;

    @Enumerated(EnumType.STRING)
    @Column(name = "Commande_Passer_Par")
    private CommandePasserPar commandePasserPar ;
    @Basic
    @Column(name = "Duree_Licence")
    private Duree dureeLicence;

    @Basic
    @Column(name = "Nom_du_Contact")
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
    @Column(name = "Remarques")
    private String remarques;
    
    @Column(name = "fichier")
    private String fichier;

    @Column(name = "fichier_original_name")
    private String fichierOriginalName;
    
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

}



