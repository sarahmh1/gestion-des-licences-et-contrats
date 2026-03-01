package com.example.projet2024.controller;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.fortinet.Fortinet;

import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.FortinetServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/Fortinet")
@CrossOrigin(origins = "http://localhost:4200")
public class FortinetController {

    @Autowired
    private FortinetServiceImpl fortinetService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private FortinetServiceImpl fortinetServiceImpl;

    // Répertoire de stockage des fichiers
    private final Path fileStorageLocation = Paths.get("uploads/fortinet-files").toAbsolutePath().normalize();

    @PostMapping("/addFortinet")
    public Fortinet addFortinet(@RequestBody Fortinet fortinet) {
        return fortinetService.addFortinet(fortinet);
    }

    @GetMapping("/check-expirations")
    public ResponseEntity<String> checkExpirationsManuellement() {
        System.out.println(">> Check expiration Fortinet appelé");
        try {
            fortinetService.checkForExpiringFortinets();
            return ResponseEntity.ok("Vérification des licences Fortinet expirantes exécutée avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la vérification des licences : " + e.getMessage());
        }
    }

    @PutMapping("/updateFortinet")
    public ResponseEntity<?> updateFortinet(@RequestBody Fortinet fortinet) {
        if (fortinet.getFortinetId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'ID du Fortinet est requis.");
        }

        Fortinet existingFortinet = fortinetService.retrieveFortinet(fortinet.getFortinetId());
        if (existingFortinet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Fortinet avec ID " + fortinet.getFortinetId() + " non trouvé.");
        }

        try {
            Fortinet updated = fortinetService.updateFortinet(fortinet);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Fortinet> getFortinetById(@PathVariable("id") Long id) {
        try {
            Fortinet fortinet = fortinetService.retrieveFortinet(id);
            if (fortinet != null) {
                System.out.println("Fortinet trouvé: " + fortinet.getClient());
                System.out.println("Licences: " + (fortinet.getLicences() != null ? fortinet.getLicences().size() : "null"));
                if (fortinet.getLicences() != null) {
                    fortinet.getLicences().forEach(lic -> System.out.println("  - Licence: " + lic.getNomDesLicences()));
                }
                return ResponseEntity.ok(fortinet);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/allFortinet")
    public List<Fortinet> getAllFortinets() {
        return fortinetService.retrieveAllFortinets();
    }

    @DeleteMapping("/delete/{Fortinet-id}")
    public void deleteById(@PathVariable("Fortinet-id") Long fortinetId) {
        fortinetService.deleteById(fortinetId);
    }

    @PutMapping("/approuve/{id}")
    public void activateFortinet(@PathVariable("id") Long id) {
        fortinetService.activate(id);
    }

    // ==================== GESTION DES FICHIERS ====================

    // Upload de fichier pour un Fortinet
    @PostMapping("/{id}/upload-file")
    public ResponseEntity<Map<String, Object>> uploadFortinetFile(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Créer le répertoire s'il n'existe pas
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            
            Fortinet fortinet = fortinetService.retrieveFortinet(id);
            if (fortinet == null) {
                response.put("success", false);
                response.put("message", "Fortinet non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Supprimer l'ancien fichier s'il existe
            if (fortinet.getFichier() != null && !fortinet.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = fileStorageLocation.resolve(fortinet.getFichier());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.out.println("Impossible de supprimer l'ancien fichier: " + e.getMessage());
                }
            }
            
            // Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = "fortinet_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
            
            // Sauvegarder le fichier
            Path targetLocation = fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Mettre à jour le Fortinet avec le nom du fichier (sans toucher aux licences)
            fortinetService.updateFortinetFile(id, newFilename, originalFilename);
            
            response.put("success", true);
            response.put("message", "Fichier uploadé avec succès");
            response.put("fichier", newFilename);
            response.put("originalName", originalFilename);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Erreur lors de l'upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Télécharger un fichier Fortinet par ID (avec nom original)
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFortinetFileById(@PathVariable("id") Long id) {
        try {
            Fortinet fortinet = fortinetService.retrieveFortinet(id);
            if (fortinet == null || fortinet.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }
            
            Path filePath = fileStorageLocation.resolve(fortinet.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                // Utiliser le nom original du fichier pour le téléchargement
                String downloadFilename = fortinet.getFichierOriginalName() != null ? 
                        fortinet.getFichierOriginalName() : resource.getFilename();
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFilename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Supprimer un fichier Fortinet
    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<Map<String, Object>> deleteFortinetFile(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Fortinet fortinet = fortinetService.retrieveFortinet(id);
            if (fortinet == null) {
                response.put("success", false);
                response.put("message", "Fortinet non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (fortinet.getFichier() != null && !fortinet.getFichier().isEmpty()) {
                Path filePath = fileStorageLocation.resolve(fortinet.getFichier());
                Files.deleteIfExists(filePath);
                
                // Utiliser updateFortinetFile pour ne pas effacer les licences
                fortinetService.updateFortinetFile(id, null, null);
                
                response.put("success", true);
                response.put("message", "Fichier supprimé avec succès");
            } else {
                response.put("success", false);
                response.put("message", "Aucun fichier à supprimer");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
