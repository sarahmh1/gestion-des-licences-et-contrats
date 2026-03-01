package com.example.projet2024.controller;

import com.example.projet2024.entite.Varonis;
import com.example.projet2024.service.IVaronisService;
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
@RequestMapping("/Varonis")
@CrossOrigin(origins = "http://localhost:4200")
public class VaronisController {

    private static final String FILE_DIRECTORY = "uploads/varonis-files/";

    @Autowired
    private IVaronisService varonisService;

    @PostMapping("/addVaronis")
    public Varonis addVaronis(@RequestBody Varonis varonis) {
        return varonisService.addVaronis(varonis);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Varonis> getVaronisById(@PathVariable("id") Long id) {
        Varonis varonis = varonisService.retrieveVaronis(id);
        if (varonis != null) {
            return ResponseEntity.ok(varonis);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/allVaronis")
    public List<Varonis> getAllVaroniss() {
        return varonisService.retrieveAllVaroniss();
    }

    @PutMapping("/updateVaronis")
    public ResponseEntity<?> updateVaronis(@RequestBody Varonis varonis) {
        if (varonis.getVaronisId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'ID du Varonis est requis.");
        }
        try {
            Varonis updated = varonisService.updateVaronis(varonis);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{Varonis-id}")
    public void deleteById(@PathVariable("Varonis-id") Long varonisId) {
        varonisService.deleteById(varonisId);
    }

    @PutMapping("/approuve/{id}")
    public void activateVaronis(@PathVariable("id") Long id) {
        varonisService.activate(id);
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

            Varonis varonis = varonisService.retrieveVaronis(id);
            if (varonis == null) {
                response.put("success", false);
                response.put("message", "Varonis non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Supprimer l'ancien fichier s'il existe
            if (varonis.getFichier() != null && !varonis.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = uploadPath.resolve(varonis.getFichier());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.out.println("Impossible de supprimer l'ancien fichier: " + e.getMessage());
                }
            }

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(uniqueFilename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            varonisService.updateVaronisFile(id, uniqueFilename, originalFilename);
            Varonis updatedVaronis = varonisService.retrieveVaronis(id);

            response.put("success", true);
            response.put("message", "Fichier uploadé avec succès");
            response.put("data", updatedVaronis);
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
            Varonis varonis = varonisService.retrieveVaronis(id);
            if (varonis == null || varonis.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(FILE_DIRECTORY).resolve(varonis.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "application/octet-stream";
                String originalFileName = varonis.getFichierOriginalName() != null ? 
                    varonis.getFichierOriginalName() : varonis.getFichier();

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
            Varonis varonis = varonisService.retrieveVaronis(id);
            if (varonis == null || varonis.getFichier() == null) {
                response.put("success", false);
                response.put("message", "Varonis ou fichier non trouvé");
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(FILE_DIRECTORY).resolve(varonis.getFichier()).normalize();
            Files.deleteIfExists(filePath);

            varonisService.updateVaronisFile(id, null, null);
            Varonis updatedVaronis = varonisService.retrieveVaronis(id);

            response.put("success", true);
            response.put("message", "Fichier supprimé avec succès");
            response.put("data", updatedVaronis);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

