package com.example.projet2024.controller;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.secpoint.SecPoint;
import com.example.projet2024.entite.vmware.VMware;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.SecPointServiceImpl;
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
@RequestMapping("/SecPoint")
@CrossOrigin(origins = "http://localhost:4200")
public class SecPointController {

    @Autowired
    private SecPointServiceImpl secPointService;
    @Autowired
    private EmailService emailService;

    private final Path fileStorageLocation = Paths.get("uploads/secpoint-files").toAbsolutePath().normalize();

    public SecPointController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory for file uploads.", ex);
        }
    }

    @PostMapping("/addSecPoint")
    public SecPoint addSecPoint(@RequestBody SecPoint secPoint) {
        return secPointService.addSecPoint(secPoint);
    }

    @PutMapping("/updateSecPoint")
    public SecPoint updateSecPoint(@RequestBody SecPoint secPoint) {
        return secPointService.updateSecPoint(secPoint);
    }

    @GetMapping("/get/{id-SecPoint}")
    public SecPoint getById(@PathVariable("id-SecPoint") Long secPointId) {
        return secPointService.retrieveSecPoint(secPointId);
    }

    @GetMapping("/allSecPoint")
    public List<SecPoint> getAllSecPoints() {
        return secPointService.retrieveAllSecPoints();
    }

    @DeleteMapping("/delete/{SecPoint-id}")
    public void deleteById(@PathVariable("SecPoint-id") Long secPointId) {
        secPointService.deleteById(secPointId);
    }

    @PutMapping("/approuve/{id}")
    public void activateSecPoint(@PathVariable("id") Long id) {
        secPointService.activate(id);
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<SecPoint> uploadFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            SecPoint secPoint = secPointService.retrieveSecPoint(id);
            if (secPoint == null) {
                return ResponseEntity.notFound().build();
            }

            // Supprimer l'ancien fichier s'il existe
            if (secPoint.getFichier() != null) {
                Path oldFilePath = this.fileStorageLocation.resolve(secPoint.getFichier()).normalize();
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

            // Mettre à jour l'entité
            SecPoint updated = secPointService.updateSecPointFile(id, newFileName, originalFileName);
            return ResponseEntity.ok(updated);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        try {
            SecPoint secPoint = secPointService.retrieveSecPoint(id);
            if (secPoint == null || secPoint.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(secPoint.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + secPoint.getFichierOriginalName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<SecPoint> deleteFile(@PathVariable("id") Long id) {
        try {
            SecPoint secPoint = secPointService.retrieveSecPoint(id);
            if (secPoint == null) {
                return ResponseEntity.notFound().build();
            }

            // Supprimer le fichier physique
            if (secPoint.getFichier() != null) {
                Path filePath = this.fileStorageLocation.resolve(secPoint.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            // Mettre à jour l'entité
            SecPoint updated = secPointService.updateSecPointFile(id, null, null);
            return ResponseEntity.ok(updated);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
