package com.example.projet2024.controller;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.OneIdentity;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.OneIdentityServiceImpl;
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
@RequestMapping("/OneIdentity")
@CrossOrigin(origins = "http://localhost:4200")
public class OneIdentityController {
    @Autowired
    private EmailService emailService;
    @Autowired
    private OneIdentityServiceImpl oneIdentityService;
    
    private final Path fileStorageLocation = Paths.get("uploads/oneidentity-files").toAbsolutePath().normalize();

    public OneIdentityController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossible de créer le répertoire de stockage des fichiers OneIdentity.", ex);
        }
    }

    @PostMapping("/addOneIdentity")
    public OneIdentity addOneIdentity(@RequestBody OneIdentity oneIdentity) {
        return oneIdentityService.addOneIdentity(oneIdentity);
    }

    @PutMapping("/updateOneIdentity")
    public OneIdentity updateOeIdentity(@RequestBody OneIdentity oneIdentity) {
        return oneIdentityService.updateOneIdentity(oneIdentity);
    }

    @GetMapping("/get/{id-OneIdentity}")
    public OneIdentity getById(@PathVariable("id-OneIdentity") Long oneIdentityId) {
        return oneIdentityService.retrieveOneIdentity(oneIdentityId);
    }

    @GetMapping("/allOneIdentity")
    public List<OneIdentity> getAllOneIdentitys() {
        return oneIdentityService.retrieveAllOneIdentitys();
    }

    @DeleteMapping("/delete/{OneIdentity-id}")
    public void deleteById(@PathVariable("OneIdentity-id") Long oneIdentityId) {
        oneIdentityService.deleteById(oneIdentityId);
    }

    @PutMapping("/approuve/{id}")
    public void activateOneIdentity(@PathVariable("id") Long id) {
        oneIdentityService.activate(id);
    }

    // Upload fichier pour un OneIdentity existant
    @PostMapping("/{id}/upload")
    public ResponseEntity<OneIdentity> uploadFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            OneIdentity oneIdentity = oneIdentityService.retrieveOneIdentity(id);
            if (oneIdentity == null) {
                return ResponseEntity.notFound().build();
            }

            // Supprimer l'ancien fichier s'il existe
            if (oneIdentity.getFichier() != null && !oneIdentity.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = this.fileStorageLocation.resolve(oneIdentity.getFichier()).normalize();
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
            oneIdentity.setFichier(newFileName);
            oneIdentity.setFichierOriginalName(originalFileName);
            OneIdentity updatedOneIdentity = oneIdentityService.updateOneIdentityFile(oneIdentity);

            return ResponseEntity.ok(updatedOneIdentity);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du téléchargement du fichier", e);
        }
    }

    // Télécharger un fichier
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        try {
            OneIdentity oneIdentity = oneIdentityService.retrieveOneIdentity(id);
            if (oneIdentity == null || oneIdentity.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(oneIdentity.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "application/octet-stream";
                String originalFileName = oneIdentity.getFichierOriginalName() != null ? 
                    oneIdentity.getFichierOriginalName() : oneIdentity.getFichier();

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Supprimer un fichier
    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<OneIdentity> deleteFile(@PathVariable("id") Long id) {
        try {
            OneIdentity oneIdentity = oneIdentityService.retrieveOneIdentity(id);
            if (oneIdentity == null) {
                return ResponseEntity.notFound().build();
            }

            if (oneIdentity.getFichier() != null && !oneIdentity.getFichier().isEmpty()) {
                Path filePath = this.fileStorageLocation.resolve(oneIdentity.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            oneIdentity.setFichier(null);
            oneIdentity.setFichierOriginalName(null);
            OneIdentity updatedOneIdentity = oneIdentityService.updateOneIdentityFile(oneIdentity);

            return ResponseEntity.ok(updatedOneIdentity);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier", e);
        }
    }
}


