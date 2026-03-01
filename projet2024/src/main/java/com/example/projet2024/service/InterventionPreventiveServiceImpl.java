package com.example.projet2024.service;

import com.example.projet2024.entite.InterventionPreventive;
import com.example.projet2024.entite.IntervenantPreventif;
import com.example.projet2024.Enum.StatutInterventionPreventive;
import com.example.projet2024.repository.InterventionPreventiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class InterventionPreventiveServiceImpl implements IInterventionPreventiveService {

    private static final Logger logger = LoggerFactory.getLogger(InterventionPreventiveServiceImpl.class);

    @Autowired
    private InterventionPreventiveRepository interventionPreventiveRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public List<InterventionPreventive> getAllInterventionsPreventives() {
        return interventionPreventiveRepository.findAll();
    }

    @Override
    public InterventionPreventive getInterventionPreventiveById(Long id) {
        return interventionPreventiveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Intervention Préventive non trouvée avec l'id: " + id));
    }

    @Override
    public InterventionPreventive addInterventionPreventive(InterventionPreventive intervention) {
        // Initialiser les flags d'email à false pour permettre l'envoi des
        // notifications
        intervention.setEmailSent1WeekBefore(false);
        intervention.setEmailSent1MonthBefore(false);
        intervention.setEmailSentDayOf(false);

        // Associer les intervenants à l'intervention
        if (intervention.getIntervenants() != null) {
            for (IntervenantPreventif intervenant : intervention.getIntervenants()) {
                intervenant.setInterventionPreventive(intervention);
            }
        }
        return interventionPreventiveRepository.save(intervention);
    }

    @Override
    public InterventionPreventive updateInterventionPreventive(Long id, InterventionPreventive intervention) {
        InterventionPreventive existing = getInterventionPreventiveById(id);

        existing.setNomClient(intervention.getNomClient());
        existing.setNbInterventionsParAn(intervention.getNbInterventionsParAn());
        existing.setPeriodeDe(intervention.getPeriodeDe());
        existing.setPeriodeA(intervention.getPeriodeA());
        existing.setPeriodeRecommandeDe(intervention.getPeriodeRecommandeDe());
        existing.setPeriodeRecommandeA(intervention.getPeriodeRecommandeA());
        existing.setDateInterventionExigee(intervention.getDateInterventionExigee());
        existing.setDateIntervention(intervention.getDateIntervention());
        existing.setDateRapportPreventive(intervention.getDateRapportPreventive());
        existing.setContrat(intervention.getContrat());
        existing.setStatut(intervention.getStatut());
        existing.setEmailCommercial(intervention.getEmailCommercial());
        existing.setCcMail(intervention.getCcMail());

        // Mettre à jour les intervenants
        existing.getIntervenants().clear();
        if (intervention.getIntervenants() != null) {
            for (IntervenantPreventif intervenant : intervention.getIntervenants()) {
                intervenant.setInterventionPreventive(existing);
                existing.getIntervenants().add(intervenant);
            }
        }

        return interventionPreventiveRepository.save(existing);
    }

    @Override
    public void deleteInterventionPreventive(Long id) {
        interventionPreventiveRepository.deleteById(id);
    }

    @Override
    public List<InterventionPreventive> searchInterventionsPreventives(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllInterventionsPreventives();
        }
        return interventionPreventiveRepository.findByNomClientContainingIgnoreCase(searchTerm);
    }

    @Override
    public List<InterventionPreventive> getByContratId(Long contratId) {
        return interventionPreventiveRepository.findByContratContratId(contratId);
    }

    @Override
    public void updateInterventionPreventiveFile(Long id, String fichier, String fichierOriginalName) {
        InterventionPreventive intervention = getInterventionPreventiveById(id);
        intervention.setFichier(fichier);
        intervention.setFichierOriginalName(fichierOriginalName);
        interventionPreventiveRepository.save(intervention);
    }

    /**
     * Vérifie les interventions préventives et envoie des notifications email
     * - 1 semaine avant periodeRecommandeDe
     * - 1 mois avant periodeRecommandeA
     * - Le jour de periodeRecommandeA
     */
    @Transactional
    @Scheduled(cron = "*/10 * * * * ?") // Test: toutes les 10 secondes (changer en "0 0 8 * * ?" pour production)
    public void checkForInterventionPreventiveNotifications() {
        logger.info("=== DEBUT Vérification des notifications Intervention Préventive ===");
        LocalDate today = LocalDate.now();
        logger.info("Date du jour: {}", today);
        List<InterventionPreventive> interventions = interventionPreventiveRepository.findAll();
        logger.info("Nombre total d'interventions préventives: {}", interventions.size());

        for (InterventionPreventive intervention : interventions) {
            logger.info(
                    "Intervention ID: {}, Client: {}, PeriodeRecommandeDe: {}, PeriodeRecommandeA: {}, EmailCommercial: {}, Statut: {}, EmailSent1WeekBefore: {}, EmailSent1MonthBefore: {}, EmailSentDayOf: {}",
                    intervention.getInterventionPreventiveId(), intervention.getNomClient(),
                    intervention.getPeriodeRecommandeDe(), intervention.getPeriodeRecommandeA(),
                    intervention.getEmailCommercial(), intervention.getStatut(),
                    intervention.getEmailSent1WeekBefore(), intervention.getEmailSent1MonthBefore(),
                    intervention.getEmailSentDayOf());

            // Vérifier si l'intervention nécessite une notification
            // Condition: dateIntervention est null OU statut != TERMINE
            boolean needsNotification = intervention.getDateIntervention() == null
                    || intervention.getStatut() != StatutInterventionPreventive.TERMINE;

            if (!needsNotification) {
                logger.info("Intervention {} ignorée: intervention terminée",
                        intervention.getInterventionPreventiveId());
                continue;
            }

            // Vérifier si un email commercial est configuré
            if (intervention.getEmailCommercial() == null || intervention.getEmailCommercial().isEmpty()) {
                logger.info("Intervention {} ignorée: emailCommercial manquant",
                        intervention.getInterventionPreventiveId());
                continue;
            }

            // 1. Rappel 1 semaine avant periodeRecommandeDe (plage de tolérance 5-9 jours)
            if (intervention.getPeriodeRecommandeDe() != null) {
                long daysUntilStart = ChronoUnit.DAYS.between(today, intervention.getPeriodeRecommandeDe());
                logger.info("Intervention {} - Jours avant periodeRecommandeDe: {}",
                        intervention.getInterventionPreventiveId(), daysUntilStart);
                if (daysUntilStart >= 5 && daysUntilStart <= 9 && (intervention.getEmailSent1WeekBefore() == null
                        || !intervention.getEmailSent1WeekBefore())) {
                    logger.info(">>> Envoi email 1 semaine avant pour intervention {} (jours restants: {})",
                            intervention.getInterventionPreventiveId(), daysUntilStart);
                    sendInterventionNotificationEmail(intervention, "1_WEEK_BEFORE");
                    intervention.setEmailSent1WeekBefore(true);
                    interventionPreventiveRepository.save(intervention);
                    logger.info("Email envoyé avec succès pour intervention {} - 1 semaine avant",
                            intervention.getInterventionPreventiveId());
                }
            }

            // 2. Rappel 1 mois avant periodeRecommandeA (plage de tolérance 28-32 jours)
            if (intervention.getPeriodeRecommandeA() != null) {
                long daysUntilEnd = ChronoUnit.DAYS.between(today, intervention.getPeriodeRecommandeA());
                logger.info("Intervention {} - Jours avant periodeRecommandeA: {}",
                        intervention.getInterventionPreventiveId(), daysUntilEnd);
                if (daysUntilEnd >= 28 && daysUntilEnd <= 32 && (intervention.getEmailSent1MonthBefore() == null
                        || !intervention.getEmailSent1MonthBefore())) {
                    logger.info(">>> Envoi email 1 mois avant pour intervention {} (jours restants: {})",
                            intervention.getInterventionPreventiveId(), daysUntilEnd);
                    sendInterventionNotificationEmail(intervention, "1_MONTH_BEFORE");
                    intervention.setEmailSent1MonthBefore(true);
                    interventionPreventiveRepository.save(intervention);
                    logger.info("Email envoyé avec succès pour intervention {} - 1 mois avant",
                            intervention.getInterventionPreventiveId());
                }
            }

            // 3. Rappel le jour de periodeRecommandeA (plage de tolérance 0-1 jours)
            if (intervention.getPeriodeRecommandeA() != null) {
                long daysUntilEnd = ChronoUnit.DAYS.between(today, intervention.getPeriodeRecommandeA());
                if (daysUntilEnd >= 0 && daysUntilEnd <= 1
                        && (intervention.getEmailSentDayOf() == null || !intervention.getEmailSentDayOf())) {
                    logger.info(">>> Envoi email JOUR J pour intervention {} (jours restants: {})",
                            intervention.getInterventionPreventiveId(), daysUntilEnd);
                    sendInterventionNotificationEmail(intervention, "DAY_OF");
                    intervention.setEmailSentDayOf(true);
                    interventionPreventiveRepository.save(intervention);
                    logger.info("Email envoyé avec succès pour intervention {} - jour de la fin",
                            intervention.getInterventionPreventiveId());
                } else if (daysUntilEnd >= 0 && daysUntilEnd <= 1) {
                    logger.info("Intervention {} - Email jour J déjà envoyé (emailSentDayOf={})",
                            intervention.getInterventionPreventiveId(), intervention.getEmailSentDayOf());
                }
            }
        }
        logger.info("=== FIN Vérification des notifications Intervention Préventive ===");
    }

    /**
     * Envoie un email de notification pour une intervention préventive
     */
    private void sendInterventionNotificationEmail(InterventionPreventive intervention, String notificationType) {
        String subject;
        String urgencyText;
        String actionText;

        switch (notificationType) {
            case "1_WEEK_BEFORE":
                subject = "🔔 Rappel: Intervention Préventive à planifier dans 1 semaine - "
                        + intervention.getNomClient();
                urgencyText = "La période recommandée pour l'intervention commence dans <strong>1 semaine</strong>.";
                actionText = "Veuillez planifier l'intervention préventive dès que possible.";
                break;
            case "1_MONTH_BEFORE":
                subject = "⚠️ Rappel: Intervention Préventive - 1 mois avant fin de période - "
                        + intervention.getNomClient();
                urgencyText = "La période recommandée pour l'intervention se termine dans <strong>1 mois</strong>.";
                actionText = "Veuillez vous assurer que l'intervention est planifiée et sera réalisée avant la fin de la période.";
                break;
            case "DAY_OF":
                subject = "🚨 URGENT: Fin de période recommandée pour Intervention Préventive - "
                        + intervention.getNomClient();
                urgencyText = "La période recommandée pour l'intervention se termine <strong>AUJOURD'HUI</strong>.";
                actionText = "Action immédiate requise: l'intervention doit être réalisée immédiatement.";
                break;
            default:
                return;
        }

        String htmlContent = buildInterventionNotificationHtml(intervention, urgencyText, actionText);

        try {
            logger.info("==> Appel emailService.sendEsetNotification pour intervention {}...",
                    intervention.getInterventionPreventiveId());
            logger.info("==> Email destinataire: {}", intervention.getEmailCommercial());
            logger.info("==> CC Mails: {}", intervention.getCcMail());
            List<String> ccMails = intervention.getCcMail() != null ? intervention.getCcMail() : List.of();
            emailService.sendEsetNotification(
                    intervention.getEmailCommercial(),
                    ccMails,
                    subject,
                    htmlContent);
            logger.info("==> Email envoyé avec SUCCÈS pour intervention {} - Type: {}",
                    intervention.getInterventionPreventiveId(), notificationType);
        } catch (Exception e) {
            logger.error("==> ERREUR lors de l'envoi de l'email pour intervention {}: {}",
                    intervention.getInterventionPreventiveId(), e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Construit le contenu HTML de l'email de notification
     */
    private String buildInterventionNotificationHtml(InterventionPreventive intervention, String urgencyText,
            String actionText) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 20px; background-color: #f9f9f9; }");
        html.append(
                ".alert { background-color: #e74c3c; color: white; padding: 15px; margin: 15px 0; border-radius: 5px; }");
        html.append(".info-table { width: 100%; border-collapse: collapse; margin-top: 15px; }");
        html.append(".info-table td { padding: 10px; border-bottom: 1px solid #ddd; }");
        html.append(".info-table td:first-child { font-weight: bold; width: 40%; background-color: #ecf0f1; }");
        html.append(".footer { text-align: center; padding: 20px; color: #7f8c8d; font-size: 12px; }");
        html.append("</style></head><body>");

        html.append("<div class='container'>");
        html.append("<div class='header'><h2>🔧 Intervention Préventive - Notification</h2></div>");
        html.append("<div class='content'>");

        html.append("<div class='alert'><p>").append(urgencyText).append("</p>");
        html.append("<p>").append(actionText).append("</p></div>");

        html.append("<h3>Détails de l'intervention:</h3>");
        html.append("<table class='info-table'>");
        html.append("<tr><td>Client</td><td>")
                .append(intervention.getNomClient() != null ? intervention.getNomClient() : "-").append("</td></tr>");
        html.append("<tr><td>Nombre d'interventions/an</td><td>")
                .append(intervention.getNbInterventionsParAn() != null ? intervention.getNbInterventionsParAn() : "-")
                .append("</td></tr>");
        html.append("<tr><td>Période (contrat)</td><td>")
                .append(formatDateRange(intervention.getPeriodeDe(), intervention.getPeriodeA())).append("</td></tr>");
        html.append("<tr><td>Période recommandée</td><td>")
                .append(formatDateRange(intervention.getPeriodeRecommandeDe(), intervention.getPeriodeRecommandeA()))
                .append("</td></tr>");
        html.append("<tr><td>Date d'intervention exigée</td><td>")
                .append(intervention.getDateInterventionExigee() != null
                        ? intervention.getDateInterventionExigee().toString()
                        : "Non définie")
                .append("</td></tr>");
        html.append("<tr><td>Date d'intervention</td><td>")
                .append(intervention.getDateIntervention() != null ? intervention.getDateIntervention().toString()
                        : "<span style='color:red'>Non planifiée</span>")
                .append("</td></tr>");
        html.append("<tr><td>Statut</td><td>")
                .append(intervention.getStatut() != null ? intervention.getStatut().toString() : "-")
                .append("</td></tr>");
        html.append("</table>");

        html.append("</div>");
        html.append("<div class='footer'>");
        html.append(
                "<p>Ce message a été envoyé automatiquement par le système de gestion des interventions préventives.</p>");
        html.append("</div>");
        html.append("</div></body></html>");

        return html.toString();
    }

    /**
     * Formate une plage de dates pour l'affichage
     */
    private String formatDateRange(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return "-";
        }
        String fromStr = from != null ? from.toString() : "?";
        String toStr = to != null ? to.toString() : "?";
        return fromStr + " → " + toStr;
    }
}
