package com.example.projet2024.controller;

import com.example.projet2024.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    @Autowired
    private VeeamRepo veeamRepo;
    @Autowired private FortinetRepo fortinetRepo;
    @Autowired private PaloRepo paloRepo;
    @Autowired private EsetRepo esetRepo;
    @Autowired private F5Repo f5Repo;
    @Autowired private CiscoRepo ciscoRepo;
    @Autowired private AlwarebytesRepo alwarebytesRepo;
    @Autowired private BitdefenderRepo bitdefenderRepo;
    @Autowired private FortraRepo fortraRepo;
    @Autowired private ImpervaRepo impervaRepo;
    @Autowired private InfobloxRepo infobloxRepo;
    @Autowired private MicrosoftO365Repo microsoftO365Repo;
    @Autowired private NetskopeRepo netskopeRepo;
    @Autowired private OneIdentityRepo oneIdentityRepo;
    @Autowired private ProofpointRepo proofpointRepo;
    @Autowired private Rapid7Repo rapid7Repo;
    @Autowired private SecPointRepo secPointRepo;
    @Autowired private SentineIOneRepo sentineIOneRepo;
    @Autowired private SplunkRepo splunkRepo;
    @Autowired private VaronisRepo varonisRepo;
    @Autowired private VMwareRepo vMwareRepo;
    @Autowired private WallixRepo wallixRepo;
    @Autowired private CrowdstrikeRepo crowdstrikeRepo;

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("Veeam", veeamRepo.count());
        stats.put("Fortinet", fortinetRepo.count());
        stats.put("Palo", paloRepo.count());
        stats.put("Eset", esetRepo.count());
        stats.put("Fortra", fortraRepo.count());
        stats.put("F5", f5Repo.count());
        stats.put("VMware", vMwareRepo.count());
        stats.put("Wallix", wallixRepo.count()); // Correction: wallixRepo au lieu de esetRepo
        stats.put("Proofpoint", proofpointRepo.count()); // Correction: Proofpoint au lieu de ProoPoint
        stats.put("Imperva", impervaRepo.count());
        stats.put("Netskope", netskopeRepo.count());
        stats.put("Alwarebytes", alwarebytesRepo.count());
        stats.put("MicrosoftO365", microsoftO365Repo.count()); // Correction: MicrosoftO365 au lieu de Microsoft0365
        stats.put("SentineIOne", sentineIOneRepo.count());
        stats.put("SecPoint", secPointRepo.count());
        stats.put("Cisco", ciscoRepo.count());
        stats.put("OneIdentity", oneIdentityRepo.count());
        stats.put("Varonis", varonisRepo.count());
        stats.put("Splunk", splunkRepo.count());
        stats.put("Infoblox", infobloxRepo.count());
        // Suppression du doublon "Eset"
        stats.put("Rapid7", rapid7Repo.count());
        stats.put("bitdefender", bitdefenderRepo.count());
        stats.put("crowdstrike", crowdstrikeRepo.count());
        // Suppression du doublon "Wallix"

        return stats;
    }

    @GetMapping("/expiring-by-month")
    public Map<String, Long> getExpiringByMonth() {
        Map<String, Long> expiringStats = new HashMap<>();
        LocalDate now = LocalDate.now();

        for (int i = 0; i < 12; i++) {
            LocalDate targetMonth = now.plusMonths(i);
            String monthKey = targetMonth.getYear() + "-" +
                    String.format("%02d", targetMonth.getMonthValue());

            // Compte TOUTES les licences de tous les produits qui expirent ce mois
            Long totalCount =
                    varonisRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            fortinetRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            paloRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            veeamRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            rapid7Repo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            vMwareRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            wallixRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            infobloxRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            impervaRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            f5Repo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            fortraRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            ciscoRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            alwarebytesRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            bitdefenderRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            microsoftO365Repo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            netskopeRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            oneIdentityRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            proofpointRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            secPointRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            sentineIOneRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()) +
                            splunkRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue());
                            esetRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue());
                            crowdstrikeRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue());

            expiringStats.put(monthKey, totalCount);
        }

        return expiringStats;
    }

    @GetMapping("/expiring-details")
    public Map<String, Map<String, Long>> getExpiringDetailsByMonth() {
        Map<String, Map<String, Long>> expiringDetails = new HashMap<>();
        LocalDate now = LocalDate.now();

        for (int i = 0; i < 12; i++) {
            LocalDate targetMonth = now.plusMonths(i);
            String monthKey = targetMonth.getYear() + "-" +
                    String.format("%02d", targetMonth.getMonthValue());

            Map<String, Long> productCounts = new HashMap<>();

            // Compte les licences par produit pour ce mois
            productCounts.put("Varonis", varonisRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Fortinet", fortinetRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Palo", paloRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Veeam", veeamRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Rapid7", rapid7Repo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("VMware", vMwareRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Wallix", wallixRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Infoblox", infobloxRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Imperva", impervaRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("F5", f5Repo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Fortra", fortraRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Cisco", ciscoRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Alwarebytes", alwarebytesRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Bitdefender", bitdefenderRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("MicrosoftO365", microsoftO365Repo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Netskope", netskopeRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("OneIdentity", oneIdentityRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Proofpoint", proofpointRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("SecPoint", secPointRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("SentineIOne", sentineIOneRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Splunk", splunkRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("Eset", esetRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));
            productCounts.put("crowdstrike", crowdstrikeRepo.countExpiringInMonth(targetMonth.getYear(), targetMonth.getMonthValue()));

            // Filtrer pour ne garder que les produits avec au moins 1 licence expirante
            Map<String, Long> filteredCounts = new HashMap<>();
            for (Map.Entry<String, Long> entry : productCounts.entrySet()) {
                if (entry.getValue() > 0) {
                    filteredCounts.put(entry.getKey(), entry.getValue());
                }
            }

            if (!filteredCounts.isEmpty()) {
                expiringDetails.put(monthKey, filteredCounts);
            }
        }

        return expiringDetails;
    }
}