package com.example.projet2024.controller;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.fortinet.Fortinet;
import com.example.projet2024.entite.rapid7.Rapid7;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.Rapid7ServiceImpl;
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
@RequestMapping("/Rapid7")
@CrossOrigin(origins = "http://localhost:4200")
public class Rapid7Controller {

    @Autowired
    private Rapid7ServiceImpl rapid7Service;
    @Autowired
    private EmailService emailService;

    private final Path fileStorageLocation = Paths.get("uploads/rapid7-files").toAbsolutePath().normalize();

    public Rapid7Controller() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Impossible de créer le répertoire uploads/rapid7-files", ex);
        }
    }

    @PostMapping("/addRapid7")
    public Rapid7 addRapid7(@RequestBody Rapid7 rapid7) {
        return rapid7Service.addRapid7(rapid7);
    }

    @PutMapping("/updateRapid7")
    public Rapid7 updateRapid7(@RequestBody Rapid7 rapid7) {
        return rapid7Service.updateRapid7(rapid7);
    }

    @GetMapping("/get/{id-Rapid7}")
    public Rapid7 getById(@PathVariable("id-Rapid7") Long rapid7Id) {
        return rapid7Service.retrieveRapid7(rapid7Id);
    }

    @GetMapping("/allRapid7")
    public List<Rapid7> getAllRapid7s() {
        return rapid7Service.retrieveAllRapid7s();
    }

    @DeleteMapping("/delete/{Rapid7-id}")
    public void deleteById(@PathVariable("Rapid7-id") Long rapid7Id) {
        rapid7Service.deleteById(rapid7Id);
    }

    @PutMapping("/approuve/{id}")
    public void activateRapid7(@PathVariable("id") Long id) {
        rapid7Service.activate(id);
    }

    // ============ GESTION DES FICHIERS ============

    @PostMapping("/{id}/upload")
    public ResponseEntity<Rapid7> uploadFile(@PathVariable("id") Long rapid7Id,
                                              @RequestParam("file") MultipartFile file) {
        try {
            Rapid7 rapid7 = rapid7Service.retrieveRapid7(rapid7Id);
            if (rapid7 == null) {
                return ResponseEntity.notFound().build();
            }

            // Supprimer l'ancien fichier s'il existe
            if (rapid7.getFichier() != null && !rapid7.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = this.fileStorageLocation.resolve(rapid7.getFichier()).normalize();
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.err.println("Erreur suppression ancien fichier: " + e.getMessage());
                }
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

            // Mettre à jour uniquement les champs fichier
            Rapid7 updatedRapid7 = rapid7Service.updateRapid7File(rapid7Id, newFileName, originalFileName);

            return ResponseEntity.ok(updatedRapid7);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long rapid7Id) {
        try {
            Rapid7 rapid7 = rapid7Service.retrieveRapid7(rapid7Id);
            if (rapid7 == null || rapid7.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(rapid7.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + rapid7.getFichierOriginalName() + "\"")
                    .body(resource);
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/file")
    public ResponseEntity<Rapid7> deleteFile(@PathVariable("id") Long rapid7Id) {
        Rapid7 rapid7 = rapid7Service.retrieveRapid7(rapid7Id);
        if (rapid7 == null) {
            return ResponseEntity.notFound().build();
        }

        // Supprimer le fichier physique
        if (rapid7.getFichier() != null && !rapid7.getFichier().isEmpty()) {
            try {
                Path filePath = this.fileStorageLocation.resolve(rapid7.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Erreur suppression fichier: " + e.getMessage());
            }
        }

        // Mettre à jour uniquement les champs fichier
        Rapid7 updatedRapid7 = rapid7Service.updateRapid7File(rapid7Id, null, null);

        return ResponseEntity.ok(updatedRapid7);
    }
}
