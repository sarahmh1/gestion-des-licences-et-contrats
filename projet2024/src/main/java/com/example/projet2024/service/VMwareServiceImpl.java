package com.example.projet2024.service;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.vmware.VMware;
import com.example.projet2024.repository.LicenceFortinetRepository;
import com.example.projet2024.repository.VMwareRepo;
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
public class VMwareServiceImpl implements IVMwareService {

    @Autowired
    private VMwareRepo vmwareRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private LicenceFortinetRepository licenceFortinetRepo;

    @Override
    public VMware addVMware(VMware vmware) {
        if (vmware.getLicences() != null) {
            for (LicenceFortinet licence : vmware.getLicences()) {
                licence.setVmware(vmware);
            }
        }
        return vmwareRepo.save(vmware);
    }

    @Override
    public VMware updateVMware(VMware vmware) {
        return vmwareRepo.save(vmware);
    }

    @Override
    public VMware updateVMwareFile(VMware vmware) {
        VMware existing = vmwareRepo.findById(vmware.getVmwareId()).orElse(null);
        if (existing != null) {
            existing.setFichier(vmware.getFichier());
            existing.setFichierOriginalName(vmware.getFichierOriginalName());
            return vmwareRepo.save(existing);
        }
        return null;
    }

    @Override
    public VMware retrieveVMware(Long vmwareId) {
        return vmwareRepo.findById(vmwareId).orElse(null);
    }

    @Override
    public List<VMware> retrieveAllVMwares() {
        return vmwareRepo.findAll();
    }

    @Override
    public void deleteById(Long vmwareId) {
        vmwareRepo.deleteById(vmwareId);
    }

    @Override
    public void activate(Long id) {
        VMware vmware = vmwareRepo.findById(id).orElse(null);
        if (vmware != null) {
            vmware.setApprouve(!vmware.isApprouve());
            vmwareRepo.save(vmware);
            System.out.println("VMware ID: " + id + " est maintenant " + vmware.isApprouve());
        }
    }

    // Méthode pour vérifier la date d'expiration - Même logique qu'Alwarebytes
    @Scheduled(cron = "*/10 * * * * ?")
    public void checkForExpiringVMwares() {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusMonths(6);

        List<LicenceFortinet> expiringLicences = licenceFortinetRepo.findVmwareLicencesExpiringBetween(today, maxDate);

        // Map pour grouper par client ET par timeframe - Même logique qu'Alwarebytes
        Map<String, List<LicenceFortinet>> groupedLicencesByClientAndTimeframe = new HashMap<>();

        for (LicenceFortinet licence : expiringLicences) {
            LocalDate expiration = licence.getDateEx();
            if (expiration == null) continue;

            long daysUntilExpiration = ChronoUnit.DAYS.between(today, expiration);
            boolean shouldSend = false;

            // MÊME LOGIQUE DES DATES QU'ALWAREBYTES
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
                VMware vmware = licence.getVmware();
                if (vmware != null) {
                    String timeframe = determineTimeframe(daysUntilExpiration);
                    String groupKey = vmware.getClient() + "_" + timeframe;
                    groupedLicencesByClientAndTimeframe.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(licence);
                    licenceFortinetRepo.save(licence);
                }
            }
        }

        // Envoyer les emails groupés par client et timeframe - Même logique qu'Alwarebytes
        for (Map.Entry<String, List<LicenceFortinet>> entry : groupedLicencesByClientAndTimeframe.entrySet()) {
            String[] keyParts = entry.getKey().split("_", 2);
            String client = keyParts[0];
            String timeframeKey = keyParts[1];
            List<LicenceFortinet> licences = entry.getValue();

            if (!licences.isEmpty()) {
                String timeframeDisplay = getTimeframeDisplayName(timeframeKey);
                sendGroupedExpirationEmails(licences.get(0).getVmware(), licences, timeframeDisplay);
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

    private void sendGroupedExpirationEmails(VMware vmware, List<LicenceFortinet> licencesExpirant, String delai) {
        String destinataire = vmware.getMailAdmin();
        List<String> ccMails = vmware.getCcMail();

        String sujet = " Licences VMware expirant " + delai;

        StringBuilder contenu = new StringBuilder();
        contenu.append("<p>Bonjour,</p>");
        contenu.append("<p>Vous trouvez ci-dessous les licences qui vont expirer ").append(delai).append(" :</p>");
        contenu.append("<table style='border-collapse: collapse; width: 100%;'>")
                .append("<tr>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Client</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Nom de licence</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Quantité</th>")
                .append("<th style='border: 2px solid #ff9933; padding: 8px; background-color: #000; color: #ff9933;'>Date d'expiration</th>")
                .append("</tr>");

        for (LicenceFortinet licence : licencesExpirant) {
            contenu.append("<tr>")
                    .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(vmware.getClient()).append("</td>")
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
            System.out.println("Email envoyé pour " + licencesExpirant.size() + " licences VMware expirant " + delai);
        } catch (Exception e) {
            System.err.println("Erreur envoi email VMware: " + e.getMessage());
        }
    }

    // Envoi d'un email individuel si un seul produit est concerné
    public void sendIndividualExpirationEmails(LicenceFortinet licence) {
        VMware vmware = licence.getVmware();
        if (vmware == null) {
            throw new IllegalArgumentException("La licence ne contient pas d'objet VMware associé");
        }

        LocalDate expirationDate = licence.getDateEx();
        long daysUntilExpiration = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
        String timeframe = getTimeframeDisplayName(determineTimeframe(daysUntilExpiration));

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<p>Bonjour,</p>")
                .append("<p>Vous trouverez ci-dessous les détails de la licence VMware expirant ")
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
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(vmware.getClient()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getNomDesLicences()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(licence.getQuantite()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(expirationDate).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(vmware.getNomDuContact()).append("</td>")
                .append("<td style='border: 1px solid #ff9933; padding: 8px;'>").append(vmware.getAdresseEmailContact()).append("</td>")
                .append("</tr>")
                .append("</table>");

        String subject = "Alerte: Licence VMware expirant " + timeframe;

        try {
            emailService.sendEsetNotification(
                    vmware.getMailAdmin(),
                    vmware.getCcMail() != null ? vmware.getCcMail() : List.of(),
                    subject,
                    emailBody.toString()
            );
            System.out.println("Email individuel envoyé pour la licence VMware expirant " + timeframe);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi du mail individuel VMware", e);
        }
    }
}