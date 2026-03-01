package com.example.projet2024.service;

import com.example.projet2024.entite.ESETFR;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.example.projet2024.entite.MailStructure;
import com.example.projet2024.repository.EsetFRRepo;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
@Service
public class EsetFrServiceImpl implements IEsetFRSetvice  {
    @Autowired
    private EsetFRRepo esetFRRepo;

    @Autowired
    private MailService mailService;

    @Override
    public ESETFR addESETFR(ESETFR eset) {
        eset.setMail(eset.getMail());
        eset.setDateEx(eset.getDateEx());
        eset.setClient(eset.getClient());
        eset.setCle_de_Licence(eset.getCle_de_Licence());
        eset.setIdentifiant(eset.getIdentifiant());
        eset.setNombre(eset.getNombre());
        eset.setNom_contact(eset.getNom_contact());
        eset.setNom_produit(eset.getNom_produit());
        eset.setMailAdmin((eset.getMailAdmin()));
        eset.setNmb_tlf(eset.getNmb_tlf());
        eset.setTypeAchat((eset.getTypeAchat()));
        eset.setConcernedPersonsEmails(eset.getConcernedPersonsEmails());
        ESETFR savedEset = esetFRRepo.save(eset);
        return savedEset;
    }

    @Override
    public ESETFR updateESETFR(ESETFR eset) {
        return esetFRRepo.save(eset);
    }

    @Override
    public ESETFR retrieveFRESET(Long esetid) {
        return esetFRRepo.findById(esetid).orElse(null) ;
    }

    @Override
    public List<ESETFR> retrieveAllESETsFR() {
        return esetFRRepo.findAll();
    }

    @Override
    public void deleteByIdFR(Long esetid) {
        esetFRRepo.deleteById(esetid);

    }

    @Override
    public void activateFR(Long id) {
        ESETFR eset = esetFRRepo.findById(id).orElse(null);  // Find ESET by ID
        if (eset != null) {  // Check if ESET is found
            eset.setApprouve(!eset.isApprouve()); // Toggle approuve field
            esetFRRepo.save(eset);  // Save the updated ESET

            // Log the approuve status to the console
            System.out.println("ESET ID: " + id + " is now " + eset.isApprouve());
        }
    }
    // Méthode pour vérifier la date d'expiration tous les jours
   // @Scheduled(cron = "*/10 * * * * ?") // Cette tâche sera exécutée chaque jour
    public void checkForExpiringEsets() {
        List<ESETFR
> allEsets = esetFRRepo.findAll();
        LocalDate currentDate = LocalDate.now();
        Map<LocalDate, List<ESETFR
>> groupedEsets = new HashMap<>();

        // Grouper les produits par date d'expiration
        for (ESETFR
 eset : allEsets) {
            LocalDate expirationDate = eset.getDateEx();
            long daysBetween = ChronoUnit.DAYS.between(currentDate, expirationDate);
            long monthsBetween = ChronoUnit.MONTHS.between(currentDate, expirationDate);
            long weeksBetween = ChronoUnit.WEEKS.between(currentDate, expirationDate);

            boolean shouldSendEmail = false;
            String timeframe = "";
            // Vérification des conditions d'expiration
            if (monthsBetween == 6 && ChronoUnit.DAYS.between(currentDate.plusMonths(6), expirationDate) == 0 && !eset.isEmailSent6Months()) {
                eset.setEmailSent6Months(true);
                timeframe = "dans 6 mois";
                shouldSendEmail = true;
            } else if (monthsBetween == 5 && ChronoUnit.DAYS.between(currentDate.plusMonths(5), expirationDate) == 0 && !eset.isEmailSent5Months()) {
                eset.setEmailSent5Months(true);
                shouldSendEmail = true;
                timeframe = "dans 5 mois";
            } else if (monthsBetween == 4 &&  ChronoUnit.DAYS.between(currentDate.plusMonths(4), expirationDate) == 0 && !eset.isEmailSent4Months()) {
                eset.setEmailSent4Months(true);
                shouldSendEmail = true;
                timeframe = "dans 4 mois";
            } else if (monthsBetween == 3 && ChronoUnit.DAYS.between(currentDate.plusMonths(3), expirationDate) == 0 && !eset.isEmailSent3Months()) {
                eset.setEmailSent3Months(true);
                shouldSendEmail = true;
                timeframe = "dans 3 mois";
            } else if (monthsBetween == 2 && ChronoUnit.DAYS.between(currentDate.plusMonths(2), expirationDate) == 0 && !eset.isEmailSent2Months()) {
                eset.setEmailSent2Months(true);
                shouldSendEmail = true;
                timeframe = "dans 2 mois";
            } else if (weeksBetween == 7  && ChronoUnit.DAYS.between(currentDate.plusWeeks(7), expirationDate) == 0 && !eset.isEmailSent7Weeks()) {
                eset.setEmailSent7Weeks(true);
                shouldSendEmail = true;
                timeframe = "dans 7 semaines";
            } else if (weeksBetween == 6 && ChronoUnit.DAYS.between(currentDate.plusWeeks(6), expirationDate) == 0 && !eset.isEmailSent6Weeks()) {
                eset.setEmailSent6Weeks(true);
                shouldSendEmail = true;
                timeframe = "dans 6 semaines";
            } else if (weeksBetween == 5 && ChronoUnit.DAYS.between(currentDate.plusWeeks(5), expirationDate) == 0 && !eset.isEmailSent5Weeks()) {
                eset.setEmailSent5Weeks(true);
                shouldSendEmail = true;
                timeframe = "dans 5 semaines";
            } else if (monthsBetween == 1 && ChronoUnit.DAYS.between(currentDate.plusMonths(1), expirationDate) == 0 && !eset.isEmailSent1Month()) {
                eset.setEmailSent1Month(true);
                shouldSendEmail = true;
                timeframe = "dans 1 mois";
            } else if (weeksBetween == 3 && ChronoUnit.DAYS.between(currentDate.plusWeeks(3), expirationDate) == 0 && !eset.isEmailSent3Weeks()) {
                eset.setEmailSent3Weeks(true);
                shouldSendEmail = true;
                timeframe = "dans 3 semaines";
            } else if (weeksBetween == 2 && ChronoUnit.DAYS.between(currentDate.plusWeeks(2), expirationDate) == 0 && !eset.isEmailSent2Weeks()) {
                eset.setEmailSent2Weeks(true);
                shouldSendEmail = true;
                timeframe = "dans 2 semaines";
            } else if (daysBetween == 7 && !eset.isEmailSent1Week()) {
                eset.setEmailSent1Week(true);
                shouldSendEmail = true;
                timeframe = "dans 1 semaine";
            } else if (daysBetween == 0 && !eset.isEmailSentDayOf()) {
                eset.setEmailSentDayOf(true);
                shouldSendEmail = true;
                timeframe = "aujourd'hui";
                /*

                 */
            }

            // Grouper les ESETs ayant la même date d'expiration si un email doit être envoyé
            if (shouldSendEmail) {
                groupedEsets.computeIfAbsent(expirationDate, k -> new ArrayList<>()).add(eset);
                esetFRRepo.save(eset); // Mettre à jour la base de données pour chaque produit
            }
        }

        // Après avoir regroupé les produits, envoyer les emails
        sendEmailsForGroupedEsets(groupedEsets);
    }

    // Méthode pour envoyer des emails groupés ou individuels en fonction de la date d'expiration
    private void sendEmailsForGroupedEsets(Map<LocalDate, List<ESETFR
>> groupedEsets) {
        for (Map.Entry<LocalDate, List<ESETFR
>> entry : groupedEsets.entrySet()) {
            LocalDate expirationDate = entry.getKey();
            List<ESETFR
> esets = entry.getValue();
            String timeframe = "";

            // Calculer le timeframe une seule fois en fonction du premier ESET dans la liste
            if (!esets.isEmpty()) {
                timeframe = determineTimeframe(expirationDate, esets.get(0));
            }

            if (esets.size() > 1) {
                sendGroupedExpirationEmails(esets, expirationDate, timeframe);
            } else {
                sendIndividualExpirationEmails(esets.get(0), expirationDate, timeframe);
            }
        }
    }

    private String determineTimeframe(LocalDate expirationDate, ESETFR
 eset) {
        LocalDate currentDate = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(currentDate, expirationDate);
        long monthsBetween = ChronoUnit.MONTHS.between(currentDate, expirationDate);
        long weeksBetween = ChronoUnit.WEEKS.between(currentDate, expirationDate);

        if (monthsBetween == 6) {
            return "dans 6 mois";
        } else if (monthsBetween == 5) {
            return "dans 5 mois";
        } else if (monthsBetween == 4) {
            return "dans 4 mois";
        } else if (monthsBetween == 3) {
            return "dans 3 mois";
        } else if (monthsBetween == 2) {
            return "dans 2 mois";
        } else if (weeksBetween == 7) {
            return "dans 7 semaines";
        } else if (weeksBetween == 6) {
            return "dans 6 semaines";
        } else if (weeksBetween == 5) {
            return "dans 5 semaines";
        } else if (monthsBetween == 1) {
            return "dans 1 mois";
        } else if (weeksBetween == 3) {
            return "dans 3 semaines";
        } else if (weeksBetween == 2) {
            return "dans 2 semaines";
        } else if (daysBetween == 7) {
            return "dans 1 semaine";
        } else if (daysBetween == 0) {
            return "aujourd'hui";
        }
        return "";
    }

    // Envoi d'emails groupés pour les produits ayant la même date d'expiration
    public void sendGroupedExpirationEmails(List<ESETFR
> esets, LocalDate expirationDate, String timeframe) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<p>Bonjour,</p>");
        emailBody.append("<p>Vous trouvez ci-dessous les licences qui vont expirer ").append(timeframe).append(" :</p>");

        // Table styling to ensure borders are visible between columns
        emailBody.append("<table style='border-collapse: collapse; width: 100%;'>");
        emailBody.append("<tr>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Client</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Identifiant</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Clé de Licence</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nom de produit</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nombre de licences</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Date d'expiration</th>")
                .append("</tr>");

        for (ESETFR
 eset : esets) {
            emailBody.append("<tr>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getClient()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getIdentifiant()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getCle_de_Licence()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getNom_produit()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getNombre()).append("</td>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getDateEx()).append("</td>")
                    .append("</tr>");
        }

        // End the table
        emailBody.append("</table>");

        // Mail setup
        MailStructure mailStructure = new MailStructure();
        mailStructure.setSubject(" Les licences ESET expirer " + timeframe);
        mailStructure.setMessage(emailBody.toString());

        for (ESETFR
 eset : esets) {
            List<String> recipients = eset.getConcernedPersonsEmails();
            for (String recipient : recipients) {
                try {
                    mailService.sendMail(recipient, mailStructure);
                } catch (MessagingException | jakarta.mail.MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("Email groupé envoyé pour les produits expirant " + timeframe);
    }


    // Envoi d'un email individuel si un seul produit est concerné
    public void sendIndividualExpirationEmails(ESETFR
 eset, LocalDate expirationDate, String timeframe) {
        eset = esetFRRepo.findById(eset.getEsetid()).orElseThrow();
        Hibernate.initialize(eset.getConcernedPersonsEmails());

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<p>Bonjour,</p>")
                .append("<p>Vous trouvez ci-dessous les licences qui vont expirer dans ")
                .append(timeframe)
                .append(" :</p>")
                // Table styling to ensure borders are visible between columns
                .append("<table style='border-collapse: collapse; width: 100%;'>")
                .append("<tr>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Client</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Identifiant</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Clé de Licence</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nom de produit</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nombre de licences</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Date d'expiration</th>")
                .append("</tr>")
                // Row data with consistent border and padding styles
                .append("<tr>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getClient()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getIdentifiant()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getCle_de_Licence()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getNom_produit()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(eset.getNombre()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(expirationDate).append("</td>")
                .append("</tr>")
                .append("</table>");

        // Mail setup
        MailStructure mailStructure = new MailStructure();
        mailStructure.setSubject("Les licences ESET expirer " + timeframe);
        mailStructure.setMessage(emailBody.toString());

        List<String> recipients = eset.getConcernedPersonsEmails();
        for (String recipient : recipients) {
            try {
                mailService.sendMail(recipient, mailStructure);
            } catch (MessagingException | jakarta.mail.MessagingException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Mail individuel envoyé pour le produit " + eset.getNom_produit() + " expiring " + timeframe);
    }




}
