package com.example.projet2024.controller;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.fortinet.Fortinet;
import com.example.projet2024.entite.splunk.Splunk;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.SplunkServiceImpl;
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
@RequestMapping("/Splunk")
@CrossOrigin(origins = "http://localhost:4200")
public class SplunkController {

    @Autowired
    private SplunkServiceImpl splunkService;
    @Autowired
    private EmailService emailService;
    
    private final Path fileStorageLocation = Paths.get("uploads/splunk-files").toAbsolutePath().normalize();

    public SplunkController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossible de créer le répertoire de stockage des fichiers Splunk.", ex);
        }
    }

    @PostMapping("/addSplunk")
   public Splunk addSplunk(@RequestBody Splunk splunk) {
        return splunkService.addSplunk(splunk);
    }

//    @PostMapping("/addSplunk")
//    public Splunk addSplunk(@RequestBody Splunk splunk) {
//
//        String sujet = "Nouvelle licence Splunk ajoutée";
//        String contenu = "Bonjour,\n\nUne nouvelle licence Splunk a été ajoutée.\n"
//                + "Client : " + splunk.getClient() + "\n"
//                + "NomDesLicences : " + splunk.getNomDesLicences() + "\n"
//                + "Date d'expiration : " + splunk.getDateEx() + "\n\n"
//                + "Cordialement,\nEquipe Technique";
//
//        emailService.sendEsetNotification(splunk.getMailAdmin(), splunk.getCcMail(), sujet, contenu);
//        return splunkService.addSplunk(splunk);
//    }

    @PutMapping("/updateSplunk")
    public Splunk updateSplunk(@RequestBody Splunk splunk) {
        return splunkService.updateSplunk(splunk);
    }

    @GetMapping("/get/{id-Splunk}")
    public Splunk getById(@PathVariable("id-Splunk") Long splunkId) {
        return splunkService.retrieveSplunk(splunkId);
    }

    @GetMapping("/allSplunk")
    public List<Splunk> getAllSplunks() {
        return splunkService.retrieveAllSplunks();
    }

    @DeleteMapping("/delete/{Splunk-id}")
    public void deleteById(@PathVariable("Splunk-id") Long splunkId) {
        splunkService.deleteById(splunkId);
    }

    @PutMapping("/approuve/{id}")
    public void activateSplunk(@PathVariable("id") Long id) {
        splunkService.activate(id);
    }

    // Upload fichier pour un Splunk existant
    @PostMapping("/{id}/upload")
    public ResponseEntity<Splunk> uploadFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            Splunk splunk = splunkService.retrieveSplunk(id);
            if (splunk == null) {
                return ResponseEntity.notFound().build();
            }

            // Supprimer l'ancien fichier s'il existe
            if (splunk.getFichier() != null && !splunk.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = this.fileStorageLocation.resolve(splunk.getFichier()).normalize();
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
            splunk.setFichier(newFileName);
            splunk.setFichierOriginalName(originalFileName);
            Splunk updatedSplunk = splunkService.updateSplunkFile(splunk);

            return ResponseEntity.ok(updatedSplunk);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du téléchargement du fichier", e);
        }
    }

    // Télécharger un fichier
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        try {
            Splunk splunk = splunkService.retrieveSplunk(id);
            if (splunk == null || splunk.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(splunk.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "application/octet-stream";
                String originalFileName = splunk.getFichierOriginalName() != null ? 
                    splunk.getFichierOriginalName() : splunk.getFichier();

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
    public ResponseEntity<Splunk> deleteFile(@PathVariable("id") Long id) {
        try {
            Splunk splunk = splunkService.retrieveSplunk(id);
            if (splunk == null) {
                return ResponseEntity.notFound().build();
            }

            if (splunk.getFichier() != null && !splunk.getFichier().isEmpty()) {
                Path filePath = this.fileStorageLocation.resolve(splunk.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            splunk.setFichier(null);
            splunk.setFichierOriginalName(null);
            Splunk updatedSplunk = splunkService.updateSplunkFile(splunk);

            return ResponseEntity.ok(updatedSplunk);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier", e);
        }
    }
}
