package com.example.projet2024.controller;

import com.example.projet2024.entite.F5;
import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.vmware.VMware;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.F5ServiceImpl;
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
@RequestMapping("/F5")
@CrossOrigin(origins = "http://localhost:4200")
public class F5Controller {
    @Autowired
    private F5ServiceImpl f5Service;
    @Autowired
    private EmailService emailService;

    private final Path fileStorageLocation = Paths.get("uploads/f5-files").toAbsolutePath().normalize();

    public F5Controller() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Impossible de créer le répertoire pour les fichiers F5", ex);
        }
    }
    @PostMapping("/addF5")
    public F5 addF5(@RequestBody F5 f5) {
        return f5Service.addF5(f5);
    }

    @PutMapping("/updateF5")
    public F5 updateF5(@RequestBody F5 f5) {
        return f5Service.updateF5(f5);
    }

    @GetMapping("/get/{id-F5}")
    public F5 getById(@PathVariable("id-F5") Long f5Id) {
        return f5Service.retrieveF5(f5Id);
    }

    @GetMapping("/allF5")
    public List<F5> getAllF5s() {
        return f5Service.retrieveAllF5s();
    }

    @DeleteMapping("/delete/{F5-id}")
    public void deleteById(@PathVariable("F5-id") Long f5Id) {
        f5Service.deleteById( f5Id);
    }

    @PutMapping("/approuve/{id}")
    public void activateF5(@PathVariable("id") Long id) {
        f5Service.activate(id);
    }

    // ===== FILE UPLOAD/DOWNLOAD/DELETE =====

    @PostMapping("/{id}/upload")
    public ResponseEntity<F5> uploadFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        F5 f5 = f5Service.retrieveF5(id);
        if (f5 == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Supprimer l'ancien fichier s'il existe
            if (f5.getFichier() != null) {
                Path oldFilePath = this.fileStorageLocation.resolve(f5.getFichier()).normalize();
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
            f5.setFichier(newFileName);
            f5.setFichierOriginalName(originalFileName);
            F5 updatedF5 = f5Service.updateF5File(f5);

            return ResponseEntity.ok(updatedF5);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        F5 f5 = f5Service.retrieveF5(id);
        if (f5 == null || f5.getFichier() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(f5.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + f5.getFichierOriginalName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<F5> deleteFile(@PathVariable("id") Long id) {
        F5 f5 = f5Service.retrieveF5(id);
        if (f5 == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            if (f5.getFichier() != null) {
                Path filePath = this.fileStorageLocation.resolve(f5.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            f5.setFichier(null);
            f5.setFichierOriginalName(null);
            F5 updatedF5 = f5Service.updateF5File(f5);

            return ResponseEntity.ok(updatedF5);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
