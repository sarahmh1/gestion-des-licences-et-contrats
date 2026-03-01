package com.example.projet2024.controller;

import com.example.projet2024.entite.MailStructure;
import com.example.projet2024.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/mail")
public class MailController {
    @Autowired
    private MailService mailService ;
    @PostMapping("/send/{mail}")
    public  String SendMail(@PathVariable String mail, @RequestBody MailStructure mailStructure) {

        try {
            mailService.sendMail(mail, mailStructure);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }

        return "succesfull " ;
    }
}
