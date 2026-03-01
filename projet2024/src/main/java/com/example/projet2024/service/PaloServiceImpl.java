package com.example.projet2024.service;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.paloalto.Palo;
import com.example.projet2024.repository.LicenceFortinetRepository;
import com.example.projet2024.repository.PaloRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaloServiceImpl implements IPaloService {

    @Autowired
    private PaloRepo paloRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private LicenceFortinetRepository licenceFortinetRepo;

    @Override
    public Palo addPalo(Palo palo) {
        if (palo.getLicences() != null) {
            for (LicenceFortinet licence : palo.getLicences()) {
                licence.setPalo(palo);
            }
        }
        return paloRepo.save(palo);
    }

    @Override
    public Palo updatePalo(Palo palo) {
        return paloRepo.save(palo);
    }

    @Override
    public Palo updatePaloFile(Long paloId, String fichier, String fichierOriginalName) {
        Palo existing = paloRepo.findById(paloId)
                .orElseThrow(() -> new RuntimeException("Palo non trouvé"));
        existing.setFichier(fichier);
        existing.setFichierOriginalName(fichierOriginalName);
        return paloRepo.save(existing);
    }

    @Override
    public Palo retrievePalo(Long paloId) {
        return paloRepo.findById(paloId).orElse(null);
    }

    @Override
    public List<Palo> retrieveAllPalos() {
        return paloRepo.findAll();
    }

    @Override
    public void deleteById(Long paloId) {
        paloRepo.deleteById(paloId);
    }

    @Override
    public void activate(Long id) {
        Palo palo = paloRepo.findById(id).orElse(null);
        if (palo != null) {
            palo.setApprouve(!palo.isApprouve());
            paloRepo.save(palo);
            System.out.println("Palo ID: " + id + " est maintenant " + palo.isApprouve());
        }
    }

    // Méthode pour vérifier la date d'expiration avec la même logique qu'Alwarebytes
    @Scheduled(cron = "*/10 * * * * ?")
    public void checkForExpiringPalos() {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusMonths(6);

        List<LicenceFortinet> expiringLicences = licenceFortinetRepo.findPaloLicencesExpiringBetween(today, maxDate);

        // Map pour grouper par client ET par timeframe (comme Alwarebytes)
        Map<String, List<LicenceFortinet>> groupedLicencesByClientAndTimeframe = new HashMap<>();

        for (LicenceFortinet licence : expiringLicences) {
            LocalDate expiration = licence.getDateEx();
            if (expiration == null) continue;

            long daysUntilExpiration = ChronoUnit.DAYS.between(today, expiration);
            boolean shouldSend = false;

            // MÊME LOGIQUE QUE ALWAREBYTES
            if (daysUntilExpiration == 0 && !licence.isEmailSentDayOf()) {
                licence.setEmailSentDayOf(true);
                shouldSend = true;
            } else if (daysUntilExpiration == 7 && !licence.isEmailSent1Week()) {
                licence.setEmailSent1Week(true);
                shouldSend = true;
            } else if (daysUntilExpiration == 14 && !licence.isEmailSent2Weeks()) {
                licence.setEmailSent2Weeks(true);
                shouldSend = true;
            } else if (daysUntilExpiration == 21 && !licence.isEmailSent3Weeks()) {
                licence.setEmailSent3Weeks(true);
                shouldSend = true;
            } else if (daysUntilExpiration >= 28 && daysUntilExpiration <= 34 && !licence.isEmailSent1Month()) {
                licence.setEmailSent1Month(true);
                shouldSend = true;
            } else if (daysUntilExpiration == 35 && !licence.isEmailSent5Weeks()) {
                licence.setEmailSent5Weeks(true);
                shouldSend = true;
            } else if (daysUntilExpiration == 42 && !licence.isEmailSent6Weeks()) {
                licence.setEmailSent6Weeks(true);
                shouldSend = true;
            } else if (daysUntilExpiration == 49 && !licence.isEmailSent7Weeks()) {
                licence.setEmailSent7Weeks(true);
                shouldSend = true;
            } else if (daysUntilExpiration >= 58 && daysUntilExpiration <= 64 && !licence.isEmailSent2Months()) {
                licence.setEmailSent2Months(true);
                shouldSend = true;
            } else if (daysUntilExpiration >= 88 && daysUntilExpiration <= 94 && !licence.isEmailSent3Months()) {
                licence.setEmailSent3Months(true);
                shouldSend = true;
            } else if (daysUntilExpiration >= 118 && daysUntilExpiration <= 124 && !licence.isEmailSent4Months()) {
                licence.setEmailSent4Months(true);
                shouldSend = true;
            } else if (daysUntilExpiration >= 148 && daysUntilExpiration <= 154 && !licence.isEmailSent5Months()) {
                licence.setEmailSent5Months(true);
                shouldSend = true;
            } else if (daysUntilExpiration >= 178 && daysUntilExpiration <= 184 && !licence.isEmailSent6Months()) {
                licence.setEmailSent6Months(true);
                shouldSend = true;
            }

            if (shouldSend) {
                Palo palo = licence.getPalo();
                if (palo != null) {
                    String timeframe = determineTimeframe(daysUntilExpiration);
                    String groupKey = palo.getClient() + "_" + timeframe;
                    groupedLicencesByClientAndTimeframe.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(licence);
                    licenceFortinetRepo.save(licence);
                }
            }
        }

        // Envoyer les emails groupés par client et timeframe (comme Alwarebytes)
        for (Map.Entry<String, List<LicenceFortinet>> entry : groupedLicencesByClientAndTimeframe.entrySet()) {
            String[] keyParts = entry.getKey().split("_", 2);
            String client = keyParts[0];
            String timeframeKey = keyParts[1];
            List<LicenceFortinet> licences = entry.getValue();

            if (!licences.isEmpty()) {
                String timeframeDisplay = getTimeframeDisplayName(timeframeKey);
                sendGroupedExpirationEmails(licences.get(0).getPalo(), licences, timeframeDisplay);
            }
        }
    }

    private String determineTimeframe(long daysUntilExpiration) {
        if (daysUntilExpiration == 0) return "aujourd_hui";
        if (daysUntilExpiration == 7) return "1_semaine";
        if (daysUntilExpiration == 14) return "2_semaines";
        if (daysUntilExpiration == 21) return "3_semaines";
        if (daysUntilExpiration >= 28 && daysUntilExpiration <= 34) return "1_mois";
        if (daysUntilExpiration == 35) return "5_semaines";
        if (daysUntilExpiration == 42) return "6_semaines";
        if (daysUntilExpiration == 49) return "7_semaines";
        if (daysUntilExpiration >= 58 && daysUntilExpiration <= 64) return "2_mois";
        if (daysUntilExpiration >= 88 && daysUntilExpiration <= 94) return "3_mois";
        if (daysUntilExpiration >= 118 && daysUntilExpiration <= 124) return "4_mois";
        if (daysUntilExpiration >= 148 && daysUntilExpiration <= 154) return "5_mois";
        if (daysUntilExpiration >= 178 && daysUntilExpiration <= 184) return "6_mois";
        return "autre";
    }

    private String getTimeframeDisplayName(String timeframeKey) {
        switch (timeframeKey) {
            case "1_semaine": return "dans 1 semaine";
            case "2_semaines": return "dans 2 semaines";
            case "3_semaines": return "dans 3 semaines";
            case "1_mois": return "dans 1 mois";
            case "5_semaines": return "dans 5 semaines";
            case "6_semaines": return "dans 6 semaines";
            case "7_semaines": return "dans 7 semaines";
            case "2_mois": return "dans 2 mois";
            case "3_mois": return "dans 3 mois";
            case "4_mois": return "dans 4 mois";
            case "5_mois": return "dans 5 mois";
            case "6_mois": return "dans 6 mois";
            case "aujourd_hui": return "aujourd'hui";
            default: return "bientôt";
        }
    }

    // Envoi d'emails groupés pour les produits ayant le même timeframe
    private void sendGroupedExpirationEmails(Palo palo, List<LicenceFortinet> licencesExpirant, String delai) {
        String destinataire = palo.getMailAdmin();
        List<String> ccMails = palo.getCcMail();

        String sujet = "Licences Paloalto expirant " + delai;

        StringBuilder contenu = new StringBuilder();
        contenu.append("<p>Bonjour,</p>");
        contenu.append("<p>Vous trouvez ci-dessous les licences qui vont expirer ").append(delai).append(" :</p>");
        contenu.append("<table style='border-collapse: collapse; width: 100%;'>")
                .append("<tr>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Client</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nom du Boîtier</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Numéro de Série</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nom de licence</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Quantité</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Date d'expiration</th>")
                .append("</tr>");

        for (LicenceFortinet licence : licencesExpirant) {
            contenu.append("<tr>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(palo.getClient()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(palo.getNomDuBoitier()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(palo.getNumeroSerieBoitier()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getNomDesLicences()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getQuantite()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getDateEx()).append("</td>")
                    .append("</tr>");
        }

        contenu.append("</table>");
        contenu.append("<p>Merci de prendre les mesures nécessaires.</p>");

        try {
            emailService.sendEsetNotification(
                    destinataire,
                    ccMails != null ? ccMails : List.of(),
                    sujet,
                    contenu.toString()
            );
            System.out.println("Email envoyé pour " + licencesExpirant.size() + " licences Palo expirant " + delai);
        } catch (Exception e) {
            System.err.println("Erreur envoi email Palo: " + e.getMessage());
        }
    }

    // Envoi d'un email individuel si un seul produit est concerné
    public void sendIndividualExpirationEmails(LicenceFortinet licence) {
        Palo palo = licence.getPalo();
        if (palo == null) {
            throw new IllegalArgumentException("La licence ne contient pas d'objet Palo associé");
        }

        LocalDate expirationDate = licence.getDateEx();
        long daysUntilExpiration = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
        String timeframe = getTimeframeDisplayName(determineTimeframe(daysUntilExpiration));

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<p>Bonjour,</p>")
                .append("<p>Vous trouverez ci-dessous les détails de la licence Palo expirant ")
                .append(timeframe)
                .append(" (le ").append(expirationDate).append(") :</p>")
                .append("<table style='border-collapse: collapse; width: 100%;'>")
                .append("<tr>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Client</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nom du Boîtier</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nom de licence</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Quantité</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Date d'expiration</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Contact</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Email contact</th>")
                .append("</tr>")
                .append("<tr>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(palo.getClient()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(palo.getNomDuBoitier()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getNomDesLicences()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getQuantite()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(expirationDate).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(palo.getNomDuContact()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(palo.getAdresseEmailContact()).append("</td>")
                .append("</tr>")
                .append("</table>");

        String subject = "Alerte: Licence Palo expirant " + timeframe;

        try {
            emailService.sendEsetNotification(
                    palo.getMailAdmin(),
                    palo.getCcMail() != null ? palo.getCcMail() : List.of(),
                    subject,
                    emailBody.toString()
            );
            System.out.println("Email individuel envoyé pour la licence Palo expirant " + timeframe);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi du mail individuel Palo", e);
        }
    }
}