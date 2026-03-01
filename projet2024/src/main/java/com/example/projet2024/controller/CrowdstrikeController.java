package com.example.projet2024.controller;


import com.example.projet2024.entite.Crowdstrike;
import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.service.CrowdstrikeServiceImpl;
import com.example.projet2024.service.EmailService;
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
@RequestMapping("/Crowdstrike")
@CrossOrigin(origins = "http://localhost:4200")
public class CrowdstrikeController {
    @Autowired
    private CrowdstrikeServiceImpl crowdstrikeService;
    @Autowired
    private EmailService emailService;

    private final Path fileStorageLocation = Paths.get("uploads/crowdstrike-files").toAbsolutePath().normalize();

    public CrowdstrikeController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Impossible de créer le répertoire uploads/crowdstrike-files", ex);
        }
    }

    @PostMapping("/addCrowdstrike")
    public Crowdstrike addCrowdstrike(@RequestBody Crowdstrike crowdstrike) {

        return crowdstrikeService.addCrowdstrike(crowdstrike);
    }

    @PutMapping("/updateCrowdstrike")
    public Crowdstrike updateCrowdstrike(@RequestBody Crowdstrike crowdstrike) {
        return crowdstrikeService.updateCrowdstrike(crowdstrike);
    }

    @GetMapping("/get/{id}")
    public Crowdstrike getById(@PathVariable("id") Long crowdstrikeId) {
        return crowdstrikeService.retrieveCrowdstrike(crowdstrikeId);
    }

    @GetMapping("/allCrowdstrike")
    public List<Crowdstrike> getAllCrowdstrikes() {
        return crowdstrikeService.retrieveAllCrowdstrikes();
    }

    @DeleteMapping("/delete/{id}")
    public void deleteById(@PathVariable("id") Long crowdstrikeId) {
        crowdstrikeService.deleteById(crowdstrikeId);
    }

    @PutMapping("/approuve/{id}")
    public void activateCrowdstrike(@PathVariable("id") Long id) {
        crowdstrikeService.activate(id);
    }

    // ============ GESTION DES FICHIERS ============

    @PostMapping("/{id}/upload")
    public ResponseEntity<Crowdstrike> uploadFile(@PathVariable("id") Long crowdstrikeId,
                                                   @RequestParam("file") MultipartFile file) {
        try {
            Crowdstrike crowdstrike = crowdstrikeService.retrieveCrowdstrike(crowdstrikeId);
            if (crowdstrike == null) {
                return ResponseEntity.notFound().build();
            }

            // Supprimer l'ancien fichier s'il existe
            if (crowdstrike.getFichier() != null && !crowdstrike.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = this.fileStorageLocation.resolve(crowdstrike.getFichier()).normalize();
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
            Crowdstrike updatedCrowdstrike = crowdstrikeService.updateCrowdstrikeFile(crowdstrikeId, newFileName, originalFileName);

            return ResponseEntity.ok(updatedCrowdstrike);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long crowdstrikeId) {
        try {
            Crowdstrike crowdstrike = crowdstrikeService.retrieveCrowdstrike(crowdstrikeId);
            if (crowdstrike == null || crowdstrike.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(crowdstrike.getFichier()).normalize();
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
                            "attachment; filename=\"" + crowdstrike.getFichierOriginalName() + "\"")
                    .body(resource);
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/file")
    public ResponseEntity<Crowdstrike> deleteFile(@PathVariable("id") Long crowdstrikeId) {
        Crowdstrike crowdstrike = crowdstrikeService.retrieveCrowdstrike(crowdstrikeId);
        if (crowdstrike == null) {
            return ResponseEntity.notFound().build();
        }

        // Supprimer le fichier physique
        if (crowdstrike.getFichier() != null && !crowdstrike.getFichier().isEmpty()) {
            try {
                Path filePath = this.fileStorageLocation.resolve(crowdstrike.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Erreur suppression fichier: " + e.getMessage());
            }
        }

        // Mettre à jour uniquement les champs fichier
        Crowdstrike updatedCrowdstrike = crowdstrikeService.updateCrowdstrikeFile(crowdstrikeId, null, null);

        return ResponseEntity.ok(updatedCrowdstrike);
    }
}
