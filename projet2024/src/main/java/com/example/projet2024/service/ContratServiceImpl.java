package com.example.projet2024.service;

import com.example.projet2024.entite.Contrat;
import com.example.projet2024.entite.DateAvenant;
import com.example.projet2024.repository.ContratRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ContratServiceImpl implements IContratService {

    private static final Logger logger = LoggerFactory.getLogger(ContratServiceImpl.class);

    @Autowired
    private ContratRepository contratRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public List<Contrat> getAllContrats() {
        return contratRepository.findAll();
    }

    @Override
    public Contrat getContratById(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat non trouvé avec l'id: " + id));
    }

    @Override
    public Contrat addContrat(Contrat contrat) {
        // Initialiser les flags d'email à false pour permettre l'envoi des notifications
        contrat.setEmailSent30Days(false);
        contrat.setEmailSentDayOf(false);
        
        // Associer les dates avenants au contrat
        if (contrat.getDatesAvenants() != null) {
            for (DateAvenant da : contrat.getDatesAvenants()) {
                da.setContrat(contrat);
            }
        }
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat updateContrat(Long id, Contrat contrat) {
        Contrat existingContrat = getContratById(id);
        
        existingContrat.setClient(contrat.getClient());
        existingContrat.setObjetContrat(contrat.getObjetContrat());
        existingContrat.setNbInterventionsPreventives(contrat.getNbInterventionsPreventives());
        existingContrat.setNbInterventionsCuratives(contrat.getNbInterventionsCuratives());
        existingContrat.setDateDebut(contrat.getDateDebut());
        existingContrat.setDateFin(contrat.getDateFin());
        existingContrat.setRenouvelable(contrat.getRenouvelable());
        existingContrat.setRemarque(contrat.getRemarque());
        existingContrat.setEmailCommercial(contrat.getEmailCommercial());
        existingContrat.setCcMail(contrat.getCcMail());
        
        // Mettre à jour les dates avenants
        existingContrat.getDatesAvenants().clear();
        if (contrat.getDatesAvenants() != null) {
            for (DateAvenant da : contrat.getDatesAvenants()) {
                da.setContrat(existingContrat);
                existingContrat.getDatesAvenants().add(da);
            }
        }
        
        return contratRepository.save(existingContrat);
    }

    @Override
    public void deleteContrat(Long id) {
        contratRepository.deleteById(id);
    }

    @Override
    public List<Contrat> searchContrats(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllContrats();
        }
        
        List<Contrat> byClient = contratRepository.findByClientContainingIgnoreCase(searchTerm);
        List<Contrat> byObjet = contratRepository.findByObjetContratContainingIgnoreCase(searchTerm);
        
        return Stream.concat(byClient.stream(), byObjet.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void updateContratFile(Long id, String fichier, String fichierOriginalName) {
        Contrat contrat = getContratById(id);
        contrat.setFichier(fichier);
        contrat.setFichierOriginalName(fichierOriginalName);
        contratRepository.save(contrat);
    }

    /**
     * Envoie un email de test pour vérifier la configuration SMTP
     */
    @Transactional
    public void sendTestEmail(Contrat contrat) {
        String sujet = "TEST - Email de test Contrat #" + contrat.getContratId();
        String contenu = "<html><body>" +
                "<h2>Email de Test</h2>" +
                "<p>Ceci est un email de test pour le contrat:</p>" +
                "<ul>" +
                "<li><strong>ID:</strong> " + contrat.getContratId() + "</li>" +
                "<li><strong>Client:</strong> " + contrat.getClient() + "</li>" +
                "<li><strong>Date Fin:</strong> " + contrat.getDateFin() + "</li>" +
                "</ul>" +
                "<p>Si vous recevez cet email, la configuration SMTP fonctionne correctement.</p>" +
                "</body></html>";
        
        List<String> ccMails = contrat.getCcMail() != null ? contrat.getCcMail() : List.of();
        
        System.out.println("=== TEST EMAIL ===");
        System.out.println("Destinataire: " + contrat.getEmailCommercial());
        System.out.println("CC: " + ccMails);
        System.out.println("Sujet: " + sujet);
        
        emailService.sendEsetNotification(
                contrat.getEmailCommercial(),
                ccMails,
                sujet,
                contenu
        );
        
        System.out.println("=== EMAIL TEST ENVOYÉ ===");
    }

    /**
     * Vérifie les contrats expirants et envoie des notifications par email.
     * - 30 jours avant la date de fin
     * - Le jour même de la fin du contrat
     */
    @Transactional
    @Scheduled(cron = "*/10 * * * * ?") // Test: toutes les 10 secondes (changer en "0 0 8 * * ?" pour production)
    public void checkForExpiringContrats() {
        logger.info("=== DEBUT Vérification des contrats expirants ===");
        LocalDate today = LocalDate.now();
        logger.info("Date du jour: {}", today);
        List<Contrat> allContrats = contratRepository.findAll();
        logger.info("Nombre total de contrats: {}", allContrats.size());

        for (Contrat contrat : allContrats) {
            logger.info("Contrat ID: {}, Client: {}, DateFin: {}, EmailCommercial: {}, EmailSentDayOf: {}", 
                contrat.getContratId(), contrat.getClient(), contrat.getDateFin(), 
                contrat.getEmailCommercial(), contrat.getEmailSentDayOf());
            
            if (contrat.getDateFin() == null || contrat.getEmailCommercial() == null || contrat.getEmailCommercial().isEmpty()) {
                logger.info("Contrat {} ignoré: dateFin ou emailCommercial manquant", contrat.getContratId());
                continue;
            }

            long daysUntilExpiration = ChronoUnit.DAYS.between(today, contrat.getDateFin());
            logger.info("Contrat {} - Jours restants: {}", contrat.getContratId(), daysUntilExpiration);

            // Notification 30 jours avant (avec plage de tolérance 28-32 jours)
            if (daysUntilExpiration >= 28 && daysUntilExpiration <= 32 && (contrat.getEmailSent30Days() == null || !contrat.getEmailSent30Days())) {
                logger.info(">>> Envoi email 30 jours pour contrat {} (jours restants: {})", contrat.getContratId(), daysUntilExpiration);
                boolean emailSent = sendExpirationEmail(contrat, (int) daysUntilExpiration);
                if (emailSent) {
                    contrat.setEmailSent30Days(true);
                    contratRepository.save(contrat);
                    logger.info("Email envoyé avec succès pour contrat {} - ~30 jours avant expiration", contrat.getContratId());
                }
            }

            // Notification le jour J (0 ou 1 jour restant)
            if (daysUntilExpiration >= 0 && daysUntilExpiration <= 1 && (contrat.getEmailSentDayOf() == null || !contrat.getEmailSentDayOf())) {
                logger.info(">>> Envoi email JOUR J pour contrat {} (jours restants: {})", contrat.getContratId(), daysUntilExpiration);
                boolean emailSent = sendExpirationEmail(contrat, (int) daysUntilExpiration);
                if (emailSent) {
                    contrat.setEmailSentDayOf(true);
                    contratRepository.save(contrat);
                    logger.info("Email envoyé avec succès pour contrat {} - jour de l'expiration", contrat.getContratId());
                }
            } else if (daysUntilExpiration >= 0 && daysUntilExpiration <= 1) {
                logger.info("Contrat {} - Email jour J déjà envoyé (emailSentDayOf={})", contrat.getContratId(), contrat.getEmailSentDayOf());
            }
        }
        logger.info("=== FIN Vérification des contrats expirants ===");
    }

    /**
     * Envoie un email de notification d'expiration de contrat.
     * @return true si l'email a été envoyé avec succès, false sinon
     */
    private boolean sendExpirationEmail(Contrat contrat, int daysRemaining) {
        logger.info("==> sendExpirationEmail appelée pour contrat {} avec {} jours restants", contrat.getContratId(), daysRemaining);
        logger.info("==> Email destinataire: {}", contrat.getEmailCommercial());
        logger.info("==> CC Mails: {}", contrat.getCcMail());
        
        String sujet;
        String urgence;
        String couleurUrgence;

        if (daysRemaining >= 28) {
            // Notification ~30 jours (plage 28-32)
            sujet = "⚠️ Rappel: Contrat expire dans " + daysRemaining + " jours - " + contrat.getClient();
            urgence = "RAPPEL - " + daysRemaining + " JOURS";
            couleurUrgence = "#f39c12";
        } else if (daysRemaining == 1) {
            sujet = "🚨 URGENT: Contrat expire DEMAIN - " + contrat.getClient();
            urgence = "EXPIRATION DEMAIN";
            couleurUrgence = "#e74c3c";
        } else {
            sujet = "🚨 URGENT: Contrat expire AUJOURD'HUI - " + contrat.getClient();
            urgence = "EXPIRATION AUJOURD'HUI";
            couleurUrgence = "#e74c3c";
        }

        String contenu = "<html><body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: " + couleurUrgence + "; color: white; padding: 15px; text-align: center;'>" +
                "<h2 style='margin: 0;'>" + urgence + "</h2>" +
                "</div>" +
                "<div style='padding: 20px;'>" +
                "<h3>Détails du Contrat</h3>" +
                "<table style='width: 100%; border-collapse: collapse;'>" +
                "<tr style='background-color: #f8f9fa;'>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'><strong>Client</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'>" + contrat.getClient() + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'><strong>Objet du Contrat</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'>" + (contrat.getObjetContrat() != null ? contrat.getObjetContrat() : "N/A") + "</td>" +
                "</tr>" +
                "<tr style='background-color: #f8f9fa;'>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'><strong>Date de Début</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'>" + (contrat.getDateDebut() != null ? contrat.getDateDebut().toString() : "N/A") + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'><strong>Date de Fin</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6; color: " + couleurUrgence + "; font-weight: bold;'>" + contrat.getDateFin().toString() + "</td>" +
                "</tr>" +
                "<tr style='background-color: #f8f9fa;'>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'><strong>Renouvelable</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'>" + (Boolean.TRUE.equals(contrat.getRenouvelable()) ? "Oui" : "Non") + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'><strong>Interventions Préventives</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'>" + (contrat.getNbInterventionsPreventives() != null ? contrat.getNbInterventionsPreventives() : "N/A") + "</td>" +
                "</tr>" +
                "<tr style='background-color: #f8f9fa;'>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'><strong>Interventions Curatives</strong></td>" +
                "<td style='padding: 10px; border: 1px solid #dee2e6;'>" + (contrat.getNbInterventionsCuratives() != null ? contrat.getNbInterventionsCuratives() : "N/A") + "</td>" +
                "</tr>" +
                "</table>" +
                (contrat.getRemarque() != null && !contrat.getRemarque().isEmpty() ? 
                    "<div style='margin-top: 15px; padding: 10px; background-color: #fff3cd; border-radius: 5px;'>" +
                    "<strong>Remarque:</strong> " + contrat.getRemarque() + "</div>" : "") +
                "<p style='margin-top: 20px; color: #666;'>Ce message est envoyé automatiquement par le système de gestion des contrats.</p>" +
                "</div>" +
                "</body></html>";

        try {
            logger.info("==> Appel emailService.sendEsetNotification...");
            List<String> ccMails = contrat.getCcMail() != null ? contrat.getCcMail() : List.of();
            emailService.sendEsetNotification(
                    contrat.getEmailCommercial(),
                    ccMails,
                    sujet,
                    contenu
            );
            System.out.println("Email envoyé pour contrat " + contrat.getContratId() + " - Client: " + contrat.getClient());
            logger.info("==> Email envoyé avec SUCCÈS pour contrat {}", contrat.getContratId());
            return true;
        } catch (Exception e) {
            System.err.println("Erreur envoi email Contrat: " + e.getMessage());
            logger.error("==> ERREUR lors de l'envoi de l'email pour le contrat {}: {}", contrat.getContratId(), e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
