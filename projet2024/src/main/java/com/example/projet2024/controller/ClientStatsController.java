package com.example.projet2024.controller;

import com.example.projet2024.dto.ClientStatsDTO;
import com.example.projet2024.entite.Contrat;
import com.example.projet2024.entite.InterventionCurative;
import com.example.projet2024.entite.InterventionPreventive;
import com.example.projet2024.entite.PeriodeLigne;
import com.example.projet2024.repository.InterventionCurativeRepository;
import com.example.projet2024.repository.InterventionPreventiveRepository;
import com.example.projet2024.service.IContratService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ClientStats")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ClientStatsController {

    @Autowired
    private IContratService contratService;

    @Autowired
    private InterventionCurativeRepository interventionCurativeRepository;

    @Autowired
    private InterventionPreventiveRepository interventionPreventiveRepository;

    @GetMapping("/all")
    public ResponseEntity<List<ClientStatsDTO>> getClientStats() {
        List<Contrat> contrats = contratService.getAllContrats();

        // Key = "client||nomProduit"
        Map<String, Double> totalCurativesByKey = new HashMap<>();
        Map<String, Double> consommeByKey = new HashMap<>();
        Map<String, Integer> totalPrevByKey = new HashMap<>();
        Map<String, Integer> consommePrevByKey = new HashMap<>();
        Map<String, String[]> keyToClientProduit = new LinkedHashMap<>();

        // 1. Aggregate curatives total from contrats
        for (Contrat contrat : contrats) {
            String client = contrat.getClient();
            if (client == null || client.trim().isEmpty())
                continue;
            client = client.trim();
            String produit = contrat.getNomProduit() != null ? contrat.getNomProduit() : "";
            String key = client + "||" + produit;
            keyToClientProduit.put(key, new String[] { client, produit });

            double nb = parseDuree(contrat.getNbInterventionsCuratives());
            totalCurativesByKey.merge(key, nb, Double::sum);

            int nbPrev = 0;
            if (contrat.getNbInterventionsPreventives() != null) {
                try {
                    nbPrev = Integer.parseInt(contrat.getNbInterventionsPreventives().replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {
                }
            }
            totalPrevByKey.merge(key, nbPrev, Integer::sum);
        }

        // 2. Aggregate curative consumed from interventions curatives
        List<InterventionCurative> interventions = interventionCurativeRepository.findAll();
        for (InterventionCurative intervention : interventions) {
            String client = intervention.getNomClient();
            if (client == null || client.trim().isEmpty())
                continue;
            client = client.trim();
            String produit = intervention.getNomProduit() != null ? intervention.getNomProduit() : "";
            String key = client + "||" + produit;
            if (!keyToClientProduit.containsKey(key)) {
                keyToClientProduit.put(key, new String[] { client, produit });
            }
            double duree = parseDuree(intervention.getDureeIntervention());
            consommeByKey.merge(key, duree, Double::sum);
        }

        // 3. Count consumed preventive interventions
        List<InterventionPreventive> preventives = interventionPreventiveRepository.findAll();
        for (InterventionPreventive prev : preventives) {
            String client = prev.getNomClient();
            if (client == null || client.trim().isEmpty())
                continue;
            client = client.trim();
            String produit = prev.getNomProduit() != null ? prev.getNomProduit() : "";
            String key = client + "||" + produit;
            if (!keyToClientProduit.containsKey(key)) {
                keyToClientProduit.put(key, new String[] { client, produit });
            }
            int consumed = 0;
            if (prev.getPeriodeLignes() != null) {
                for (PeriodeLigne pl : prev.getPeriodeLignes()) {
                    if (pl.getDateIntervention() != null)
                        consumed++;
                }
            }
            consommePrevByKey.merge(key, consumed, Integer::sum);
        }

        // 4. Build result list
        List<ClientStatsDTO> stats = new ArrayList<>();
        for (Map.Entry<String, String[]> entry : keyToClientProduit.entrySet()) {
            String key = entry.getKey();
            String client = entry.getValue()[0];
            String produit = entry.getValue()[1];

            double total = totalCurativesByKey.getOrDefault(key, 0.0);
            double consomme = consommeByKey.getOrDefault(key, 0.0);
            double nonConsomme = Math.max(0, total - consomme);

            int totalPrev = totalPrevByKey.getOrDefault(key, 0);
            int consommePrev = consommePrevByKey.getOrDefault(key, 0);
            int nonConsommePrev = Math.max(0, totalPrev - consommePrev);

            stats.add(new ClientStatsDTO(client, produit, total, consomme, nonConsomme,
                    totalPrev, consommePrev, nonConsommePrev));
        }

        stats.sort(Comparator.comparing(ClientStatsDTO::getClient, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Comparator.comparing(dto -> dto.getNomProduit() == null ? "" : dto.getNomProduit(),
                        String.CASE_INSENSITIVE_ORDER)));

        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    /**
     * Delete all contrats (and related data) for a given client+produit combination
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteClientStatEntry(
            @RequestParam String client,
            @RequestParam(required = false) String nomProduit) {
        List<Contrat> contrats = contratService.getAllContrats();
        for (Contrat contrat : contrats) {
            boolean clientMatch = client
                    .equalsIgnoreCase(contrat.getClient() != null ? contrat.getClient().trim() : "");
            String cp = contrat.getNomProduit() != null ? contrat.getNomProduit() : "";
            String np = nomProduit != null ? nomProduit : "";
            boolean produitMatch = np.equalsIgnoreCase(cp);
            if (clientMatch && produitMatch) {
                contratService.deleteContrat(contrat.getContratId());
            }
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Parse dureeIntervention and convert to J/H.
     * 1 J/H = 8 heures.
     *
     * Supported formats:
     * "2h" → 2 hours → 2/8 = 0.25 J/H
     * "2h30" → 2.5 hours → 2.5/8 = 0.3125 J/H
     * "2H30" → same (case-insensitive)
     * "4h" → 4/8 = 0.5 J/H
     * "8h" → 8/8 = 1 J/H
     * "3 J/H" → 3 J/H (already in J/H)
     * "3" → 3 J/H (plain number = J/H)
     * "1 jour" → 1 J/H
     */
    private double parseDuree(String duree) {
        if (duree == null || duree.trim().isEmpty())
            return 0;
        String s = duree.trim();

        // Format "XhYY" or "Xh" (hours with optional minutes) → convert to J/H
        Pattern hoursPattern = Pattern.compile("(?i)(\\d+[.,]?\\d*)\\s*h\\s*(\\d+)?");
        Matcher hoursMatcher = hoursPattern.matcher(s);
        if (hoursMatcher.find()) {
            String hoursStr = hoursMatcher.group(1).replace(",", ".");
            double hours = Double.parseDouble(hoursStr);
            if (hoursMatcher.group(2) != null) {
                double minutes = Double.parseDouble(hoursMatcher.group(2));
                hours += minutes / 60.0;
            }
            return hours / 8.0; // Convert hours to J/H
        }

        // Format "X J/H" or "X jour(s)" → already in J/H
        Pattern jhPattern = Pattern.compile("(?i)(\\d+[.,]?\\d*)\\s*(j/?h|jour|jours|j)");
        Matcher jhMatcher = jhPattern.matcher(s);
        if (jhMatcher.find()) {
            String numberStr = jhMatcher.group(1).replace(",", ".");
            return Double.parseDouble(numberStr);
        }

        // Plain number → treat as J/H
        Pattern numberPattern = Pattern.compile("(\\d+[.,]?\\d*)");
        Matcher numberMatcher = numberPattern.matcher(s);
        if (numberMatcher.find()) {
            String numberStr = numberMatcher.group(1).replace(",", ".");
            try {
                return Double.parseDouble(numberStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
