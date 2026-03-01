package com.example.projet2024.service;

import com.example.projet2024.entite.MailStructure;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender ;
    @Value("${spring.mail.username}")
    private String fromMail ;
    public void sendMail(String to, MailStructure mailStructure) throws MessagingException, jakarta.mail.MessagingException {
        // Créer un message MIME
        MimeMessage message = mailSender.createMimeMessage();

        // Aide pour configurer le message MIME
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Configurer les détails du message
        helper.setFrom(fromMail);
        helper.setTo(to);
        helper.setSubject(mailStructure.getSubject());
        helper.setText(mailStructure.getMessage(), true);  // Le paramètre `true` permet le rendu HTML

        // Envoyer le mail
        mailSender.send(message);
    }
    }




