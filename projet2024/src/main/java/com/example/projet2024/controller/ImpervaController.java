package com.example.projet2024.controller;

import com.example.projet2024.entite.Imperva;
import com.example.projet2024.service.IImpervaService;
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
@RequestMapping("/Imperva")
@CrossOrigin(origins = "http://localhost:4200")
public class ImpervaController {

    private static final String FILE_DIRECTORY = "uploads/imperva-files/";

    @Autowired
    private IImpervaService impervaService;

    @PostMapping("/addImperva")
    public Imperva addImperva(@RequestBody Imperva imperva) {
        return impervaService.addImperva(imperva);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Imperva> getImpervaById(@PathVariable("id") Long id) {
        Imperva imperva = impervaService.retrieveImperva(id);
        if (imperva != null) {
            return ResponseEntity.ok(imperva);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/allImperva")
    public List<Imperva> getAllImpervas() {
        return impervaService.retrieveAllImpervas();
    }

    @PutMapping("/updateImperva")
    public ResponseEntity<?> updateImperva(@RequestBody Imperva imperva) {
        if (imperva.getImpervaId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'ID du Imperva est requis.");
        }
        try {
            Imperva updated = impervaService.updateImperva(imperva);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{Imperva-id}")
    public void deleteById(@PathVariable("Imperva-id") Long impervaId) {
        impervaService.deleteById(impervaId);
    }

    @PutMapping("/approuve/{id}")
    public void activateImperva(@PathVariable("id") Long id) {
        impervaService.activate(id);
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

            Imperva imperva = impervaService.retrieveImperva(id);
            if (imperva == null) {
                response.put("success", false);
                response.put("message", "Imperva non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Supprimer l'ancien fichier s'il existe
            if (imperva.getFichier() != null && !imperva.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = uploadPath.resolve(imperva.getFichier());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.out.println("Impossible de supprimer l'ancien fichier: " + e.getMessage());
                }
            }

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(uniqueFilename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            impervaService.updateImpervaFile(id, uniqueFilename, originalFilename);
            Imperva updatedImperva = impervaService.retrieveImperva(id);

            response.put("success", true);
            response.put("message", "Fichier uploadé avec succès");
            response.put("data", updatedImperva);
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
            Imperva imperva = impervaService.retrieveImperva(id);
            if (imperva == null || imperva.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(FILE_DIRECTORY).resolve(imperva.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "application/octet-stream";
                String originalFileName = imperva.getFichierOriginalName() != null ? 
                    imperva.getFichierOriginalName() : imperva.getFichier();

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
            Imperva imperva = impervaService.retrieveImperva(id);
            if (imperva == null || imperva.getFichier() == null) {
                response.put("success", false);
                response.put("message", "Imperva ou fichier non trouvé");
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(FILE_DIRECTORY).resolve(imperva.getFichier()).normalize();
            Files.deleteIfExists(filePath);

            impervaService.updateImpervaFile(id, null, null);
            Imperva updatedImperva = impervaService.retrieveImperva(id);

            response.put("success", true);
            response.put("message", "Fichier supprimé avec succès");
            response.put("data", updatedImperva);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
