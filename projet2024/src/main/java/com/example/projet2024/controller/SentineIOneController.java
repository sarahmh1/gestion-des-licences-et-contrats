package com.example.projet2024.controller;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.SentineIOne;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.SentineIOneServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/SentineIOne")
@CrossOrigin(origins = "http://localhost:4200")
public class SentineIOneController {
    @Autowired
    private SentineIOneServiceImpl sentineIOneService;
    @Autowired
    private EmailService emailService;

    private final Path fileStorageLocation = Paths.get("uploads/sentineione-files").toAbsolutePath().normalize();

    public SentineIOneController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Impossible de créer le répertoire pour les fichiers SentineIOne", ex);
        }
    }

    @PostMapping("/addSentineIOne")
    public SentineIOne addSentineIOne(@RequestBody SentineIOne sentineIOne) {
        return sentineIOneService.addSentineIOne(sentineIOne);
    }

    @PutMapping("/updateSentineIOne")
    public SentineIOne updateSentineIOne(@RequestBody SentineIOne sentineIOne) {
        return sentineIOneService.updateSentineIOne(sentineIOne);
    }

    @GetMapping("/get/{id-SentineIOne}")
    public SentineIOne getById(@PathVariable("id-SentineIOne") Long sentineIOneId) {
        return sentineIOneService.retrieveSentineIOne(sentineIOneId);
    }

    @GetMapping("/allSentineIOne")
    public List<SentineIOne> getAllSentineIOnes() {
        return sentineIOneService.retrieveAllSentineIOnes();
    }

    @DeleteMapping("/delete/{SentineIOne-id}")
    public void deleteById(@PathVariable("SentineIOne-id") Long sentineIOneId) {
        sentineIOneService.deleteById(sentineIOneId);
    }

    @PutMapping("/approuve/{id}")
    public void activateSentineIOne(@PathVariable("id") Long id) { sentineIOneService.activate(id);
    }

    // ===== FILE UPLOAD/DOWNLOAD/DELETE =====

    @PostMapping("/{id}/upload")
    public ResponseEntity<SentineIOne> uploadFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        SentineIOne sentineIOne = sentineIOneService.retrieveSentineIOne(id);
        if (sentineIOne == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Supprimer l'ancien fichier s'il existe
            if (sentineIOne.getFichier() != null) {
                Path oldFilePath = this.fileStorageLocation.resolve(sentineIOne.getFichier()).normalize();
                Files.deleteIfExists(oldFilePath);
            }

            // Générer un nom unique pour le fichier
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Sauvegarder le fichier
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Mettre à jour l'entité (uniquement les champs fichier)
            sentineIOne.setFichier(newFileName);
            sentineIOne.setFichierOriginalName(originalFileName);
            SentineIOne updatedSentineIOne = sentineIOneService.updateSentineIOneFile(sentineIOne);

            return ResponseEntity.ok(updatedSentineIOne);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        SentineIOne sentineIOne = sentineIOneService.retrieveSentineIOne(id);
        if (sentineIOne == null || sentineIOne.getFichier() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(sentineIOne.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + sentineIOne.getFichierOriginalName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<SentineIOne> deleteFile(@PathVariable("id") Long id) {
        SentineIOne sentineIOne = sentineIOneService.retrieveSentineIOne(id);
        if (sentineIOne == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            if (sentineIOne.getFichier() != null) {
                Path filePath = this.fileStorageLocation.resolve(sentineIOne.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            sentineIOne.setFichier(null);
            sentineIOne.setFichierOriginalName(null);
            SentineIOne updatedSentineIOne = sentineIOneService.updateSentineIOneFile(sentineIOne);

            return ResponseEntity.ok(updatedSentineIOne);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
