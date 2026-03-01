package com.example.projet2024.controller;


import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.paloalto.Palo;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.PaloServiceImpl;
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
@RequestMapping("/Palo")
@CrossOrigin(origins = "http://localhost:4200")
public class PaloController {

    @Autowired
    private PaloServiceImpl paloService;
    @Autowired
    private EmailService emailService;

    // Répertoire de stockage des fichiers
    private final Path fileStorageLocation = Paths.get("uploads/palo-files").toAbsolutePath().normalize();

    @PostMapping("/addPalo")
    public Palo addPalo(@RequestBody Palo palo) {
        return paloService.addPalo(palo);
    }

    @PutMapping("/updatePalo")
    public Palo updatePalo(@RequestBody Palo palo) {
        return paloService.updatePalo(palo);
    }

    @GetMapping("/get/{id-Palo}")
    public Palo getById(@PathVariable("id-Palo") Long paloId) {
        return paloService.retrievePalo(paloId);
    }

    @GetMapping("/allPalo")
    public List<Palo> getAllPalos() {
        return paloService.retrieveAllPalos();
    }

    @DeleteMapping("/delete/{Palo-id}")
    public void deleteById(@PathVariable("Palo-id") Long paloId) {
        paloService.deleteById(paloId);
    }

    @PutMapping("/approuve/{id}")
    public void activatePalo(@PathVariable("id") Long id) {
        paloService.activate(id);
    }

    // ==================== GESTION DES FICHIERS ====================

    // Upload de fichier pour un Palo
    @PostMapping("/{id}/upload-file")
    public ResponseEntity<Map<String, Object>> uploadPaloFile(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Créer le répertoire s'il n'existe pas
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            
            Palo palo = paloService.retrievePalo(id);
            if (palo == null) {
                response.put("success", false);
                response.put("message", "Palo non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Supprimer l'ancien fichier s'il existe
            if (palo.getFichier() != null && !palo.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = fileStorageLocation.resolve(palo.getFichier());
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
            String newFilename = "palo_" + id + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
            
            // Sauvegarder le fichier
            Path targetLocation = fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            // Mettre à jour le Palo avec le nom du fichier (sans toucher aux licences)
            paloService.updatePaloFile(id, newFilename, originalFilename);
            
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

    // Télécharger un fichier Palo par ID (avec nom original)
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadPaloFileById(@PathVariable("id") Long id) {
        try {
            Palo palo = paloService.retrievePalo(id);
            if (palo == null || palo.getFichier() == null || palo.getFichier().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Path filePath = fileStorageLocation.resolve(palo.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Utiliser le nom original pour le téléchargement
                String downloadFilename = palo.getFichierOriginalName() != null 
                    ? palo.getFichierOriginalName() 
                    : palo.getFichier();
                
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
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

    // Supprimer un fichier Palo
    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<Map<String, Object>> deletePaloFile(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Palo palo = paloService.retrievePalo(id);
            if (palo == null) {
                response.put("success", false);
                response.put("message", "Palo non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (palo.getFichier() != null && !palo.getFichier().isEmpty()) {
                Path filePath = fileStorageLocation.resolve(palo.getFichier());
                Files.deleteIfExists(filePath);
                
                // Utiliser updatePaloFile pour ne pas effacer les licences
                paloService.updatePaloFile(id, null, null);
                
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
