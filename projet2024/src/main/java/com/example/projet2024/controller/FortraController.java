package com.example.projet2024.controller;

import com.example.projet2024.entite.Fortra;
import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.vmware.VMware;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.FortraServiceImpl;
import com.example.projet2024.service.VMwareServiceImpl;
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
@RequestMapping("/Fortra")
@CrossOrigin(origins = "http://localhost:4200")
public class FortraController {
    @Autowired
    private FortraServiceImpl fortraService;
    @Autowired
    private EmailService emailService;

    private final Path fileStorageLocation = Paths.get("uploads/fortra-files").toAbsolutePath().normalize();

    public FortraController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Impossible de créer le répertoire pour les fichiers Fortra", ex);
        }
    }

    @PostMapping("/addFortra")
    public Fortra addFortra(@RequestBody Fortra fortra) {

        return fortraService.addFortra(fortra);
    }

    @PutMapping("/updateFortra")
    public Fortra updateFortra(@RequestBody Fortra fortra) {
        return fortraService.updateFortra(fortra);
    }

    @GetMapping("/get/{id-Fortra}")
    public Fortra getById(@PathVariable("id-Fortra") Long fortraId) {
        return fortraService.retrieveFortra(fortraId);
    }

    @GetMapping("/allFortra")
    public List<Fortra> getAllFortras() {
        return fortraService.retrieveAllFortras();
    }

    @DeleteMapping("/delete/{Fortra-id}")
    public void deleteById(@PathVariable("Fortra-id") Long fortraId) {
        fortraService.deleteById(fortraId);
    }

    @PutMapping("/approuve/{id}")
    public void activateFortra(@PathVariable("id") Long id) {
        fortraService.activate(id);
    }

    // ===== FILE UPLOAD/DOWNLOAD/DELETE =====

    @PostMapping("/{id}/upload")
    public ResponseEntity<Fortra> uploadFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        Fortra fortra = fortraService.retrieveFortra(id);
        if (fortra == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Supprimer l'ancien fichier s'il existe
            if (fortra.getFichier() != null) {
                Path oldFilePath = this.fileStorageLocation.resolve(fortra.getFichier()).normalize();
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
            fortra.setFichier(newFileName);
            fortra.setFichierOriginalName(originalFileName);
            Fortra updatedFortra = fortraService.updateFortraFile(fortra);

            return ResponseEntity.ok(updatedFortra);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        Fortra fortra = fortraService.retrieveFortra(id);
        if (fortra == null || fortra.getFichier() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(fortra.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fortra.getFichierOriginalName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<Fortra> deleteFile(@PathVariable("id") Long id) {
        Fortra fortra = fortraService.retrieveFortra(id);
        if (fortra == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            if (fortra.getFichier() != null) {
                Path filePath = this.fileStorageLocation.resolve(fortra.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            fortra.setFichier(null);
            fortra.setFichierOriginalName(null);
            Fortra updatedFortra = fortraService.updateFortraFile(fortra);

            return ResponseEntity.ok(updatedFortra);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
