package com.example.projet2024.service;

import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.example.projet2024.entite.ESET;
import com.example.projet2024.entite.MailStructure;
import com.example.projet2024.repository.EsetRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsetServiceImpl implements IEsetService  {
    @Autowired
    private EsetRepo esetRepo;

    @Autowired
    private MailService mailService;

    @Autowired
    private EmailService emailService;

    @Override
    public ESET addESET(ESET eset) {
        eset.setCcMail(eset.getCcMail());
        eset.setDateEx(eset.getDateEx());
        eset.setClient(eset.getClient());
        eset.setDureeDeLicence(eset.getDureeDeLicence());
        eset.setCle_de_Licence(eset.getCle_de_Licence());
        eset.setIdentifiant(eset.getIdentifiant());
        eset.setNombre(eset.getNombre());
        eset.setNom_contact(eset.getNom_contact());
        eset.setNom_produit(eset.getNom_produit());
        eset.setMailAdmin((eset.getMailAdmin()));
        eset.setNmb_tlf(eset.getNmb_tlf());
        eset.setTypeAchat((eset.getTypeAchat()));

        // Enregistrer le produit dans la base de données
        ESET savedEset = esetRepo.save(eset);
        return savedEset;
    }

    @Override
    public ESET updateESET(ESET eset) {
        return esetRepo.save(eset);
    }

    @Override
    public ESET retrieveESET(Long esetid) {
        return esetRepo.findById(esetid).orElse(null);
    }

    @Override
    public List<ESET> retrieveAllESETs() {
        return esetRepo.findAll();
    }

    @Override
    public void deleteById(Long esetid) {
        esetRepo.deleteById(esetid);
    }

    @Override
    public void activate(Long id) {
        ESET eset = esetRepo.findById(id).orElse(null);
        if (eset != null) {
            eset.setApprouve(!eset.isApprouve());
            esetRepo.save(eset);
            System.out.println("ESET ID: " + id + " is now " + eset.isApprouve());
        }
    }

    // Méthode pour vérifier la date d'expiration
    @Scheduled(cron = "*/10 * * * * ?")
    //@Scheduled(cron = "0 0 8 * * ?")
    public void checkForExpiringEsets() {
        List<ESET> allEsets = esetRepo.findAll();
        LocalDate currentDate = LocalDate.now();
        Map<String, List<ESET>> groupedByClientAndTimeframe = new HashMap<>();

        for (ESET eset : allEsets) {
            LocalDate expirationDate = eset.getDateEx();
            if (expirationDate == null) {
                System.out.println("Date d'expiration null pour l'élément: " + eset.getEsetid());
                continue;
            }

            long daysUntilExpiration = ChronoUnit.DAYS.between(currentDate, expirationDate);

            if (daysUntilExpiration < 0) {
                continue;
            }

            String timeframe = determineTimeframe(daysUntilExpiration);
            boolean shouldSendEmail = shouldSendEmailForTimeframe(eset, timeframe);

            if (shouldSendEmail && !timeframe.equals("autre")) {
                updateEmailSentFlags(eset, timeframe);

                String client = eset.getClient();
                String groupKey = client + "_" + timeframe;

                groupedByClientAndTimeframe
                        .computeIfAbsent(groupKey, k -> new ArrayList<>())
                        .add(eset);

                esetRepo.save(eset);
            }
        }

        sendEmailsGroupedByClientAndTimeframe(groupedByClientAndTimeframe);
    }

    private String determineTimeframe(long daysUntilExpiration) {
        // Semaines exactes
        if (daysUntilExpiration == 7) return "1_semaine";
        if (daysUntilExpiration == 14) return "2_semaines";
        if (daysUntilExpiration == 21) return "3_semaines";
        if (daysUntilExpiration == 35) return "5_semaines";
        if (daysUntilExpiration == 42) return "6_semaines";
        if (daysUntilExpiration == 49) return "7_semaines";

        // Plages de jours pour les mois
        if (daysUntilExpiration >= 28 && daysUntilExpiration <= 34) return "1_mois";
        if (daysUntilExpiration >= 58 && daysUntilExpiration <= 64) return "2_mois";
        if (daysUntilExpiration >= 88 && daysUntilExpiration <= 94) return "3_mois";
        if (daysUntilExpiration >= 118 && daysUntilExpiration <= 124) return "4_mois";
        if (daysUntilExpiration >= 148 && daysUntilExpiration <= 154) return "5_mois";
        if (daysUntilExpiration >= 178 && daysUntilExpiration <= 184) return "6_mois";

        // Aujourd'hui
        if (daysUntilExpiration == 0) return "aujourd_hui";

        return "autre";
    }

    private boolean shouldSendEmailForTimeframe(ESET eset, String timeframe) {
        switch (timeframe) {
            case "1_semaine": return !eset.isEmailSent1Week();
            case "2_semaines": return !eset.isEmailSent2Weeks();
            case "3_semaines": return !eset.isEmailSent3Weeks();
            case "5_semaines": return !eset.isEmailSent5Weeks();
            case "6_semaines": return !eset.isEmailSent6Weeks();
            case "7_semaines": return !eset.isEmailSent7Weeks();
            case "1_mois": return !eset.isEmailSent1Month();
            case "2_mois": return !eset.isEmailSent2Months();
            case "3_mois": return !eset.isEmailSent3Months();
            case "4_mois": return !eset.isEmailSent4Months();
            case "5_mois": return !eset.isEmailSent5Months();
            case "6_mois": return !eset.isEmailSent6Months();
            case "aujourd_hui": return !eset.isEmailSentDayOf();
            default: return false;
        }
    }

    private void updateEmailSentFlags(ESET eset, String timeframe) {
        switch (timeframe) {
            case "1_semaine": eset.setEmailSent1Week(true); break;
            case "2_semaines": eset.setEmailSent2Weeks(true); break;
            case "3_semaines": eset.setEmailSent3Weeks(true); break;
            case "5_semaines": eset.setEmailSent5Weeks(true); break;
            case "6_semaines": eset.setEmailSent6Weeks(true); break;
            case "7_semaines": eset.setEmailSent7Weeks(true); break;
            case "1_mois": eset.setEmailSent1Month(true); break;
            case "2_mois": eset.setEmailSent2Months(true); break;
            case "3_mois": eset.setEmailSent3Months(true); break;
            case "4_mois": eset.setEmailSent4Months(true); break;
            case "5_mois": eset.setEmailSent5Months(true); break;
            case "6_mois": eset.setEmailSent6Months(true); break;
            case "aujourd_hui": eset.setEmailSentDayOf(true); break;
        }
    }

    private String getTimeframeDisplayName(String timeframeKey) {
        switch (timeframeKey) {
            case "1_semaine": return "dans 1 semaine";
            case "2_semaines": return "dans 2 semaines";
            case "3_semaines": return "dans 3 semaines";
            case "5_semaines": return "dans 5 semaines";
            case "6_semaines": return "dans 6 semaines";
            case "7_semaines": return "dans 7 semaines";
            case "1_mois": return "dans 1 mois";
            case "2_mois": return "dans 2 mois";
            case "3_mois": return "dans 3 mois";
            case "4_mois": return "dans 4 mois";
            case "5_mois": return "dans 5 mois";
            case "6_mois": return "dans 6 mois";
            case "aujourd_hui": return "aujourd'hui";
            default: return "bientôt";
        }
    }

    private void sendEmailsGroupedByClientAndTimeframe(Map<String, List<ESET>> groupedByClientAndTimeframe) {
        for (Map.Entry<String, List<ESET>> entry : groupedByClientAndTimeframe.entrySet()) {
            String[] keyParts = entry.getKey().split("_", 2);
            String client = keyParts[0];
            String timeframeKey = keyParts[1];
            List<ESET> esets = entry.getValue();

            if (!esets.isEmpty()) {
                String timeframe = getTimeframeDisplayName(timeframeKey);
                sendGroupedExpirationEmailsForClient(esets, timeframe, client);
            }
        }
    }

    public void sendGroupedExpirationEmailsForClient(List<ESET> esets, String timeframe, String client) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<p>Bonjour,</p>");
        emailBody.append("<p>Vous trouverez ci-dessous les licences qui vont expirer ")
                .append("<strong>").append(timeframe).append("</strong> :</p>");

        emailBody.append("<table style='border-collapse: collapse; width: 100%;'>");
        emailBody.append("<tr>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Client</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Identifiant</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Clé de Licence</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nom de produit</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nombre de licences</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Date d'expiration</th>")
                .append("</tr>");

        for (ESET eset : esets) {
            emailBody.append("<tr>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getClient()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getIdentifiant()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getCle_de_Licence()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getNom_produit()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getNombre()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getDateEx()).append("</td>")
                    .append("</tr>");
        }

        emailBody.append("</table>");


        MailStructure mailStructure = new MailStructure();
        mailStructure.setSubject("Licences ESET pour " + client + " expirant " + timeframe);
        mailStructure.setMessage(emailBody.toString());

        ESET firstEset = esets.get(0);
        try {
            emailService.sendEsetNotification(
                    firstEset.getMailAdmin(),
                    firstEset.getCcMail(),
                    mailStructure.getSubject(),
                    mailStructure.getMessage()
            );
            System.out.println("Email groupé envoyé pour le client " + client + " - " +
                    esets.size() + " produits expirant " + timeframe);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du mail groupé pour le client " + client + ": " + e.getMessage());
        }
    }
}