package com.example.projet2024.controller;

import com.example.projet2024.entite.microsofto365.MicrosoftO365;
import com.example.projet2024.service.IMicrosoftO365Service;
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
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/MicrosoftO365")
@CrossOrigin(origins = "http://localhost:4200")
public class MicrosoftO365Controller {

    private static final String FILE_DIRECTORY = "uploads/microsofto365-files/";

    @Autowired
    private IMicrosoftO365Service microsoftO365Service;

    @PostMapping("/addMicrosoftO365")
    public MicrosoftO365 addMicrosoftO365(@RequestBody MicrosoftO365 microsoftO365) {
        return microsoftO365Service.addMicrosoftO365(microsoftO365);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<MicrosoftO365> getMicrosoftO365ById(@PathVariable("id") Long id) {
        MicrosoftO365 microsoftO365 = microsoftO365Service.retrieveMicrosoftO365(id);
        if (microsoftO365 != null) {
            return ResponseEntity.ok(microsoftO365);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/allMicrosoftO365")
    public List<MicrosoftO365> getAllMicrosoftO365s() {
        return microsoftO365Service.retrieveAllMicrosoftO365s();
    }

    @PutMapping("/updateMicrosoftO365")
    public ResponseEntity<?> updateMicrosoftO365(@RequestBody MicrosoftO365 microsoftO365) {
        if (microsoftO365.getMicrosoftO365Id() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'ID du MicrosoftO365 est requis.");
        }
        try {
            MicrosoftO365 updated = microsoftO365Service.updateMicrosoftO365(microsoftO365);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{MicrosoftO365-id}")
    public void deleteById(@PathVariable("MicrosoftO365-id") Long microsoftO365Id) {
        microsoftO365Service.deleteById(microsoftO365Id);
    }

    @PutMapping("/approuve/{id}")
    public void activateMicrosoftO365(@PathVariable("id") Long id) {
        microsoftO365Service.activate(id);
    }

    // ==================== GESTION DES FICHIERS ====================

    @PutMapping("/{id}/upload-file")
    public ResponseEntity<Map<String, Object>> uploadFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            Path uploadPath = Paths.get(FILE_DIRECTORY);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            MicrosoftO365 microsoftO365 = microsoftO365Service.retrieveMicrosoftO365(id);
            if (microsoftO365 == null) {
                response.put("success", false);
                response.put("message", "MicrosoftO365 non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Supprimer l'ancien fichier s'il existe
            if (microsoftO365.getFichier() != null && !microsoftO365.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = uploadPath.resolve(microsoftO365.getFichier());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.out.println("Impossible de supprimer l'ancien fichier: " + e.getMessage());
                }
            }

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(uniqueFilename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            microsoftO365Service.updateMicrosoftO365File(id, uniqueFilename, originalFilename);
            MicrosoftO365 updatedMicrosoftO365 = microsoftO365Service.retrieveMicrosoftO365(id);

            response.put("success", true);
            response.put("message", "Fichier uploadé avec succès");
            response.put("data", updatedMicrosoftO365);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de l'upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        try {
            MicrosoftO365 microsoftO365 = microsoftO365Service.retrieveMicrosoftO365(id);
            if (microsoftO365 == null || microsoftO365.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(FILE_DIRECTORY).resolve(microsoftO365.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "application/octet-stream";
                String originalFileName = microsoftO365.getFichierOriginalName() != null ? 
                    microsoftO365.getFichierOriginalName() : microsoftO365.getFichier();

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            MicrosoftO365 microsoftO365 = microsoftO365Service.retrieveMicrosoftO365(id);
            if (microsoftO365 == null || microsoftO365.getFichier() == null) {
                response.put("success", false);
                response.put("message", "MicrosoftO365 ou fichier non trouvé");
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(FILE_DIRECTORY).resolve(microsoftO365.getFichier()).normalize();
            Files.deleteIfExists(filePath);

            microsoftO365Service.updateMicrosoftO365File(id, null, null);
            MicrosoftO365 updatedMicrosoftO365 = microsoftO365Service.retrieveMicrosoftO365(id);

            response.put("success", true);
            response.put("message", "Fichier supprimé avec succès");
            response.put("data", updatedMicrosoftO365);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
