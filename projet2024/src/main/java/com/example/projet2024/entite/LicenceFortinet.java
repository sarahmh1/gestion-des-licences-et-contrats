package com.example.projet2024.entite;

import com.example.projet2024.entite.fortinet.Fortinet;
import com.example.projet2024.entite.microsofto365.MicrosoftO365;
import com.example.projet2024.entite.paloalto.Palo;
import com.example.projet2024.entite.proofpoint.Proofpoint;
import com.example.projet2024.entite.rapid7.Rapid7;
import com.example.projet2024.entite.secpoint.SecPoint;
import com.example.projet2024.entite.splunk.Splunk;
import com.example.projet2024.entite.veeam.Veeam;
import com.example.projet2024.entite.vmware.VMware;
import com.example.projet2024.entite.wallix.Wallix;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "Licence_Fortinet")
public class LicenceFortinet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomDesLicences;
    private String quantite;
    private LocalDate dateEx;

    @ManyToOne
    @JoinColumn(name = "fortinet_id")
    @JsonBackReference("fortinet-licence") // <--- Remplacer ici !
    private Fortinet fortinet;
    @ManyToOne
    @JoinColumn(name = "palo_id")
    @JsonBackReference("palo-licence")  // <--- Remplacer ici !
    private Palo palo;
    @ManyToOne
    @JoinColumn(name = "veeam_id")
    @JsonBackReference("veeam-licence") // <--- Remplacer ici !
    private Veeam veeam;
    @ManyToOne
    @JoinColumn(name = "wallix_id")
    @JsonBackReference("wallix-licence") // <--- Remplacer ici !
    private Wallix wallix;
    @ManyToOne
    @JoinColumn(name = "vmware_id")
    @JsonBackReference("vmware-licence") // <--- Remplacer ici !
    private VMware vmware;
    @ManyToOne
    @JoinColumn(name = "splunk_id")
    @JsonBackReference("splunk-licence") // <--- Remplacer ici !
    private Splunk splunk;
    @ManyToOne
    @JoinColumn(name = "infoblox_id")
    @JsonBackReference("infoblox-licence") // <--- Remplacer ici !
    private Infoblox infoblox;
    @ManyToOne
    @JoinColumn(name = "varonis_id")
    @JsonBackReference("varonis-licence") // <--- Remplacer ici !
    private Varonis varonis;
    @ManyToOne
    @JoinColumn(name = "imperva_id")
    @JsonBackReference("imperva-licence") // <--- Remplacer ici !
    private Imperva imperva;
    @ManyToOne
    @JoinColumn(name = "cisco_id")
    @JsonBackReference("cisco-licence") // <--- Remplacer ici !
    private Cisco cisco;
    @ManyToOne
    @JoinColumn(name = "microsoft0365_id")
    @JsonBackReference("microsoft-licence") // <--- Remplacer ici !
    private MicrosoftO365 microsoftO365;
    @ManyToOne
    @JoinColumn(name = "rapid7_id")
    @JsonBackReference("rapid7-licence") // <--- Remplacer ici !
    private Rapid7 rapid7;
    @ManyToOne
    @JoinColumn(name = "alwarebytes_id")
    @JsonBackReference("alwarebytes-licence") // <--- Remplacer ici !
    private Alwarebytes alwarebytes;
    @ManyToOne
    @JoinColumn(name = "f5_id")
    @JsonBackReference("f5-licence") // <--- Remplacer ici !
    private F5 f5;
    @ManyToOne
    @JoinColumn(name = "fortra_id")
    @JsonBackReference("fortra-licence") // <--- Remplacer ici !
    private Fortra fortra;
    @ManyToOne
    @JoinColumn(name = "sentineIOne_id")
    @JsonBackReference("sentineIOne-licence") // <--- Remplacer ici !
    private SentineIOne sentineIOne;
    @ManyToOne
    @JoinColumn(name = "netskope_id")
    @JsonBackReference("netskope-licence") // <--- Remplacer ici !
    private Netskope netskope;
    @ManyToOne
    @JoinColumn(name = "bitdefender_id")
    @JsonBackReference("bitdefender-licence") // <--- Remplacer ici !
    private Bitdefender bitdefender;
    @ManyToOne
    @JoinColumn(name = "crowdstrike_id")
    @JsonBackReference("crowdstrike-licence") // <--- Remplacer ici !
    private Crowdstrike crowdstrike;
    @ManyToOne
    @JoinColumn(name = "oneidentity_id")
    @JsonBackReference("oneidentity-licence") // <--- Remplacer ici !
    private OneIdentity oneIdentity;
    @ManyToOne
    @JoinColumn(name = "secpoint_id")
    @JsonBackReference("secpoint-licence") // <--- Remplacer ici !
    private SecPoint secPoint;
    @ManyToOne
    @JoinColumn(name = "proofpoint_id")
    @JsonBackReference("proofpoint-licence") // <--- Remplacer ici !
    private Proofpoint proofpoint;
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
