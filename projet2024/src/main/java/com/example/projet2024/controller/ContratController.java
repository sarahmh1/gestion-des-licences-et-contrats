package com.example.projet2024.controller;

import com.example.projet2024.entite.Contrat;
import com.example.projet2024.service.ContratServiceImpl;
import com.example.projet2024.service.IContratService;
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
@RequestMapping("/Contrat")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ContratController {

    @Autowired
    private IContratService contratService;

    @Autowired
    private ContratServiceImpl contratServiceImpl;

    // Répertoire de stockage des fichiers
    private final Path fileStorageLocation = Paths.get("uploads/contrat-files").toAbsolutePath().normalize();

    @GetMapping("/all")
    public ResponseEntity<List<Contrat>> getAllContrats() {
        List<Contrat> contrats = contratService.getAllContrats();
        return new ResponseEntity<>(contrats, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contrat> getContratById(@PathVariable Long id) {
        Contrat contrat = contratService.getContratById(id);
        return new ResponseEntity<>(contrat, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Contrat> addContrat(@RequestBody Contrat contrat) {
        Contrat newContrat = contratService.addContrat(contrat);
        return new ResponseEntity<>(newContrat, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Contrat> updateContrat(@PathVariable Long id, @RequestBody Contrat contrat) {
        Contrat updatedContrat = contratService.updateContrat(id, contrat);
        return new ResponseEntity<>(updatedContrat, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteContrat(@PathVariable Long id) {
        contratService.deleteContrat(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Contrat>> searchContrats(@RequestParam String searchTerm) {
        List<Contrat> contrats = contratService.searchContrats(searchTerm);
        return new ResponseEntity<>(contrats, HttpStatus.OK);
    }

    @GetMapping("/check-expirations")
    public ResponseEntity<String> checkExpirationsManuellement() {
        System.out.println(">> Check expiration Contrat appelé");
        try {
            contratServiceImpl.checkForExpiringContrats();
            return ResponseEntity.ok("Vérification des contrats expirants exécutée avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la vérification des contrats : " + e.getMessage());
        }
    }

    // Debug: voir l'état de tous les contrats
    @GetMapping("/debug-all")
    public ResponseEntity<String> debugAllContrats() {
        List<Contrat> contrats = contratService.getAllContrats();
        StringBuilder sb = new StringBuilder();
        sb.append("=== DEBUG CONTRATS ===\n");
        sb.append("Date du jour: ").append(java.time.LocalDate.now()).append("\n\n");
        
        for (Contrat c : contrats) {
            sb.append("ID: ").append(c.getContratId()).append("\n");
            sb.append("  Client: ").append(c.getClient()).append("\n");
            sb.append("  DateFin: ").append(c.getDateFin()).append("\n");
            sb.append("  EmailCommercial: ").append(c.getEmailCommercial()).append("\n");
            sb.append("  CcMail: ").append(c.getCcMail()).append("\n");
            sb.append("  emailSent30Days: ").append(c.getEmailSent30Days()).append("\n");
            sb.append("  emailSentDayOf: ").append(c.getEmailSentDayOf()).append("\n");
            if (c.getDateFin() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), c.getDateFin());
                sb.append("  Jours restants: ").append(days).append("\n");
            }
            sb.append("\n");
        }
        return ResponseEntity.ok(sb.toString());
    }

    // TEST: Envoyer un email de test pour un contrat spécifique (sans modifier les flags)
    @GetMapping("/{id}/test-email")
    public ResponseEntity<String> testSendEmail(@PathVariable Long id) {
        try {
            Contrat contrat = contratService.getContratById(id);
            if (contrat.getEmailCommercial() == null || contrat.getEmailCommercial().isEmpty()) {
                return ResponseEntity.badRequest().body("Ce contrat n'a pas d'email commercial défini");
            }
            
            contratServiceImpl.sendTestEmail(contrat);
            return ResponseEntity.ok("Email de test envoyé à " + contrat.getEmailCommercial());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }

    // Réinitialiser les flags d'email d'un contrat (pour tests)
    @PutMapping("/{id}/reset-email-flags")
    public ResponseEntity<String> resetEmailFlags(@PathVariable Long id) {
        try {
            Contrat contrat = contratService.getContratById(id);
            contrat.setEmailSent30Days(false);
            contrat.setEmailSentDayOf(false);
            contratService.updateContrat(id, contrat);
            return ResponseEntity.ok("Flags email réinitialisés pour le contrat " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }

    // ==================== GESTION DES FICHIERS ====================

    // Upload de fichier pour un Contrat
    @PutMapping("/{id}/upload-file")
    public ResponseEntity<Map<String, Object>> uploadContratFile(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Créer le répertoire s'il n'existe pas
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            
            Contrat contrat = contratService.getContratById(id);
            if (contrat == null) {
                response.put("success", false);
                response.put("message", "Contrat non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Supprimer l'ancien fichier s'il existe
            if (contrat.getFichier() != null && !contrat.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = fileStorageLocation.resolve(contrat.getFichier());
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
            String newFilename = "contrat_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
            
            // Sauvegarder le fichier
            Path targetLocation = fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Mettre à jour le Contrat avec le nom du fichier
            contratService.updateContratFile(id, newFilename, originalFilename);
            
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

    // Télécharger un fichier Contrat par ID
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadContratFile(@PathVariable("id") Long id) {
        try {
            Contrat contrat = contratService.getContratById(id);
            if (contrat == null || contrat.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }
            
            Path filePath = fileStorageLocation.resolve(contrat.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                // Utiliser le nom original du fichier pour le téléchargement
                String downloadFilename = contrat.getFichierOriginalName() != null ? 
                        contrat.getFichierOriginalName() : resource.getFilename();
                
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

    // Supprimer un fichier Contrat
    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<Map<String, Object>> deleteContratFile(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Contrat contrat = contratService.getContratById(id);
            if (contrat == null) {
                response.put("success", false);
                response.put("message", "Contrat non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (contrat.getFichier() != null && !contrat.getFichier().isEmpty()) {
                Path filePath = fileStorageLocation.resolve(contrat.getFichier());
                Files.deleteIfExists(filePath);
                contratService.updateContratFile(id, null, null);
            }
            
            response.put("success", true);
            response.put("message", "Fichier supprimé avec succès");
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
