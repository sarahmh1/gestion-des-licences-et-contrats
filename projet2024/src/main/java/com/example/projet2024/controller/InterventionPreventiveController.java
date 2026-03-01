package com.example.projet2024.controller;

import com.example.projet2024.entite.InterventionPreventive;
import com.example.projet2024.service.IInterventionPreventiveService;
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
@RequestMapping("/InterventionPreventive")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class InterventionPreventiveController {

    @Autowired
    private IInterventionPreventiveService interventionPreventiveService;

    // Répertoire de stockage des fichiers
    private final Path fileStorageLocation = Paths.get("uploads/intervention-preventive-files").toAbsolutePath().normalize();

    @GetMapping("/all")
    public ResponseEntity<List<InterventionPreventive>> getAllInterventionsPreventives() {
        List<InterventionPreventive> interventions = interventionPreventiveService.getAllInterventionsPreventives();
        return new ResponseEntity<>(interventions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterventionPreventive> getInterventionPreventiveById(@PathVariable Long id) {
        InterventionPreventive intervention = interventionPreventiveService.getInterventionPreventiveById(id);
        return new ResponseEntity<>(intervention, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<InterventionPreventive> addInterventionPreventive(@RequestBody InterventionPreventive intervention) {
        InterventionPreventive newIntervention = interventionPreventiveService.addInterventionPreventive(intervention);
        return new ResponseEntity<>(newIntervention, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<InterventionPreventive> updateInterventionPreventive(@PathVariable Long id, @RequestBody InterventionPreventive intervention) {
        InterventionPreventive updatedIntervention = interventionPreventiveService.updateInterventionPreventive(id, intervention);
        return new ResponseEntity<>(updatedIntervention, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteInterventionPreventive(@PathVariable Long id) {
        interventionPreventiveService.deleteInterventionPreventive(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/search")
    public ResponseEntity<List<InterventionPreventive>> searchInterventionsPreventives(@RequestParam String searchTerm) {
        List<InterventionPreventive> interventions = interventionPreventiveService.searchInterventionsPreventives(searchTerm);
        return new ResponseEntity<>(interventions, HttpStatus.OK);
    }

    @GetMapping("/contrat/{contratId}")
    public ResponseEntity<List<InterventionPreventive>> getByContratId(@PathVariable Long contratId) {
        List<InterventionPreventive> interventions = interventionPreventiveService.getByContratId(contratId);
        return new ResponseEntity<>(interventions, HttpStatus.OK);
    }

    // ==================== GESTION DES FICHIERS ====================

    // Upload de fichier pour une Intervention Préventive
    @PutMapping("/{id}/upload-file")
    public ResponseEntity<Map<String, Object>> uploadInterventionPreventiveFile(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Créer le répertoire s'il n'existe pas
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            
            InterventionPreventive intervention = interventionPreventiveService.getInterventionPreventiveById(id);
            if (intervention == null) {
                response.put("success", false);
                response.put("message", "Intervention Préventive non trouvée");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Supprimer l'ancien fichier s'il existe
            if (intervention.getFichier() != null && !intervention.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = fileStorageLocation.resolve(intervention.getFichier());
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
            String newFilename = "intervention_preventive_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
            
            // Sauvegarder le fichier
            Path targetLocation = fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Mettre à jour l'Intervention avec le nom du fichier
            interventionPreventiveService.updateInterventionPreventiveFile(id, newFilename, originalFilename);
            
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

    // Télécharger un fichier Intervention Préventive par ID
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadInterventionPreventiveFile(@PathVariable("id") Long id) {
        try {
            InterventionPreventive intervention = interventionPreventiveService.getInterventionPreventiveById(id);
            if (intervention == null || intervention.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }
            
            Path filePath = fileStorageLocation.resolve(intervention.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                // Utiliser le nom original du fichier pour le téléchargement
                String downloadFilename = intervention.getFichierOriginalName() != null ? 
                        intervention.getFichierOriginalName() : resource.getFilename();
                
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

    // Supprimer un fichier Intervention Préventive
    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<Map<String, Object>> deleteInterventionPreventiveFile(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            InterventionPreventive intervention = interventionPreventiveService.getInterventionPreventiveById(id);
            if (intervention == null) {
                response.put("success", false);
                response.put("message", "Intervention Préventive non trouvée");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (intervention.getFichier() != null && !intervention.getFichier().isEmpty()) {
                Path filePath = fileStorageLocation.resolve(intervention.getFichier());
                Files.deleteIfExists(filePath);
                interventionPreventiveService.updateInterventionPreventiveFile(id, null, null);
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
