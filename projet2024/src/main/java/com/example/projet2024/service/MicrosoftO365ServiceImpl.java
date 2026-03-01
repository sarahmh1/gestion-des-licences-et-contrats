package com.example.projet2024.service;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.microsofto365.MicrosoftO365;
import com.example.projet2024.repository.LicenceFortinetRepository;
import com.example.projet2024.repository.MicrosoftO365Repo;
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
public class MicrosoftO365ServiceImpl implements IMicrosoftO365Service {

    @Autowired
    private MicrosoftO365Repo microsoftO365Repo;
    @Autowired
    private LicenceFortinetRepository licenceFortinetRepo;
    @Autowired
    private EmailService emailService;

    @Override
    public MicrosoftO365 addMicrosoftO365(MicrosoftO365 microsoftO365) {
        if (microsoftO365.getLicences() != null) {
            for (LicenceFortinet licence : microsoftO365.getLicences()) {
                licence.setMicrosoftO365(microsoftO365);
            }
        }
        return microsoftO365Repo.save(microsoftO365);
    }

    @Override
    public MicrosoftO365 updateMicrosoftO365(MicrosoftO365 microsoftO365) {
        return microsoftO365Repo.save(microsoftO365);
    }

    @Override
    public MicrosoftO365 retrieveMicrosoftO365(Long microsoftO365Id) {
        return microsoftO365Repo.findById(microsoftO365Id).orElse(null);
    }

    @Override
    public List<MicrosoftO365> retrieveAllMicrosoftO365s() {
        return microsoftO365Repo.findAll();
    }

    @Override
    public void deleteById(Long microsoftO365Id) {
        microsoftO365Repo.deleteById(microsoftO365Id);
    }

    @Override
    public void activate(Long id) {
        MicrosoftO365 microsoftO365 = microsoftO365Repo.findById(id).orElse(null);
        if (microsoftO365 != null) {
            microsoftO365.setApprouve(!microsoftO365.isApprouve());
            microsoftO365Repo.save(microsoftO365);
            System.out.println("MicrosoftO365 ID: " + id + " est maintenant " + microsoftO365.isApprouve());
        }
    }

    @Override
    public void updateMicrosoftO365File(Long id, String fichier, String fichierOriginalName) {
        MicrosoftO365 microsoftO365 = microsoftO365Repo.findById(id).orElse(null);
        if (microsoftO365 != null) {
            microsoftO365.setFichier(fichier);
            microsoftO365.setFichierOriginalName(fichierOriginalName);
            microsoftO365Repo.save(microsoftO365);
        }
    }

    // Méthode pour vérifier la date d'expiration
    @Scheduled(cron = "*/10 * * * * ?")
    public void checkForExpiringMicrosofts() {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusMonths(6);

        List<LicenceFortinet> expiringLicences = licenceFortinetRepo.findMicrosoftLicencesExpiringBetween(today, maxDate);

        // Map pour grouper par client ET par timeframe (comme dans Alwarebytes)
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
                MicrosoftO365 microsoftO365 = licence.getMicrosoftO365();
                if (microsoftO365 != null) {
                    String timeframe = determineTimeframe(daysUntilExpiration);
                    String groupKey = microsoftO365.getClient() + "_" + timeframe;
                    groupedLicencesByClientAndTimeframe.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(licence);
                    licenceFortinetRepo.save(licence);
                }
            }
        }

        // Envoyer les emails groupés par client et timeframe (comme dans Alwarebytes)
        for (Map.Entry<String, List<LicenceFortinet>> entry : groupedLicencesByClientAndTimeframe.entrySet()) {
            String[] keyParts = entry.getKey().split("_", 2);
            String client = keyParts[0];
            String timeframeKey = keyParts[1];
            List<LicenceFortinet> licences = entry.getValue();

            if (!licences.isEmpty()) {
                String timeframeDisplay = getTimeframeDisplayName(timeframeKey);
                sendGroupedExpirationEmails(licences.get(0).getMicrosoftO365(), licences, timeframeDisplay);
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

    // Méthode modifiée pour accepter le paramètre delai (comme dans Alwarebytes)
    private void sendGroupedExpirationEmails(MicrosoftO365 microsoftO365, List<LicenceFortinet> licencesExpirant, String delai) {
        String destinataire = microsoftO365.getMailAdmin();
        List<String> ccMails = microsoftO365.getCcMail();

        String sujet = "Licences Microsoft 365 expirant " + delai;

        StringBuilder contenu = new StringBuilder();
        contenu.append("<p>Bonjour,</p>");
        contenu.append("<p>Vous trouvez ci-dessous les licences  qui vont expirer ").append(delai).append(" :</p>");
        contenu.append("<table style='border-collapse: collapse; width: 100%;'>")
                .append("<tr>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Client</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nom de licence</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Quantité</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Date d'expiration</th>")
                .append("</tr>");

        for (LicenceFortinet licence : licencesExpirant) {
            contenu.append("<tr>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(microsoftO365.getClient()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getNomDesLicences()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getQuantite()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getDateEx()).append("</td>")
                    .append("</tr>");
        }

        contenu.append("</table>");

        try {
            emailService.sendEsetNotification(
                    destinataire,
                    ccMails != null ? ccMails : List.of(),
                    sujet,
                    contenu.toString()
            );
            System.out.println("Email envoyé pour " + licencesExpirant.size() + " licences Microsoft 365 expirant " + delai);
        } catch (Exception e) {
            System.err.println("Erreur envoi email Microsoft 365: " + e.getMessage());
        }
    }

    // Envoi d'un email individuel si un seul produit est concerné
    public void sendIndividualExpirationEmails(LicenceFortinet licence) {
        MicrosoftO365 microsoftO365 = licence.getMicrosoftO365();
        if (microsoftO365 == null) {
            throw new IllegalArgumentException("La licence ne contient pas d'objet Microsoft 365 associé");
        }

        LocalDate expirationDate = licence.getDateEx();
        long daysUntilExpiration = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
        String timeframe = getTimeframeDisplayName(determineTimeframe(daysUntilExpiration));

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<p>Bonjour,</p>")
                .append("<p>Vous trouverez ci-dessous les détails de la licence Microsoft 365 expirant ")
                .append(timeframe)
                .append(" (le ").append(expirationDate).append(") :</p>")
                .append("<table style='border-collapse: collapse; width: 100%;'>")
                .append("<tr>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Client</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nom de licence</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Quantité</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Date d'expiration</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Contact</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Email contact</th>")
                .append("</tr>")
                .append("<tr>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(microsoftO365.getClient()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getNomDesLicences()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getQuantite()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(expirationDate).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(microsoftO365.getNomDuContact()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(microsoftO365.getAdresseEmailContact()).append("</td>")
                .append("</tr>")
                .append("</table>");

        String subject = "Alerte: Licence Microsoft 365 expirant " + timeframe;

        try {
            emailService.sendEsetNotification(
                    microsoftO365.getMailAdmin(),
                    microsoftO365.getCcMail() != null ? microsoftO365.getCcMail() : List.of(),
                    subject,
                    emailBody.toString()
            );
            System.out.println("Email individuel envoyé pour la licence Microsoft 365 expirant " + timeframe);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi du mail individuel Microsoft 365", e);
        }
    }
}