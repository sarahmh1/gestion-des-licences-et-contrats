package com.example.projet2024.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.MessagingException;


import java.util.List;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendVerificationEmail(String email, String token) throws MailException {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("gerinfo2025@gmail.com");
            message.setTo(email);
            message.setSubject("Vérification de votre compte");
            message.setText(
                    "Bonjour,\n\n" +
                            "Pour vérifier votre compte, veuillez cliquer sur le lien suivant :\n" +
                            "http://localhost:8089/api/auth/verify?token=" + token + "\n\n" +
                            "Cordialement,\n" +
                            "L'équipe Technique"
            );

            javaMailSender.send(message);
            logger.info("Email de vérification envoyé à : {}", email);

        } catch (MailException e) {
            logger.error("Échec d'envoi de l'email à {} : {}", email, e.getMessage());
            throw e; // À gérer dans votre controller
        }
    }
    public void sendEsetNotification(String adminEmail, List<String> ccEmails, String sujet, String contenu) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("alerte.licence@geranceinformatique.com");
            helper.setTo(adminEmail);

            if (ccEmails != null && !ccEmails.isEmpty()) {
                helper.setCc(ccEmails.toArray(new String[0]));
            }

            helper.setSubject(sujet);

            // Important : deuxième argument 'true' = contenu HTML
            helper.setText(contenu, true);

            javaMailSender.send(message);

            logger.info("Notification ESET envoyée à admin: {} avec CC: {}", adminEmail, ccEmails);
        } catch (MailException | MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'e-mail ESET : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }



}