package com.example.projet2024.controller;

import com.example.projet2024.entite.Cisco;
import com.example.projet2024.service.ICiscoService;
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
@RequestMapping("/Cisco")
@CrossOrigin(origins = "http://localhost:4200")
public class CiscoController {

    private static final String FILE_DIRECTORY = "uploads/cisco-files/";

    @Autowired
    private ICiscoService ciscoService;

    @PostMapping("/addCisco")
    public Cisco addCisco(@RequestBody Cisco cisco) {
        return ciscoService.addCisco(cisco);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Cisco> getCiscoById(@PathVariable("id") Long id) {
        Cisco cisco = ciscoService.retrieveCisco(id);
        if (cisco != null) {
            return ResponseEntity.ok(cisco);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/allCisco")
    public List<Cisco> getAllCiscos() {
        return ciscoService.retrieveAllCiscos();
    }

    @PutMapping("/updateCisco")
    public ResponseEntity<?> updateCisco(@RequestBody Cisco cisco) {
        if (cisco.getCiscoId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'ID du Cisco est requis.");
        }
        try {
            Cisco updated = ciscoService.updateCisco(cisco);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{Cisco-id}")
    public void deleteById(@PathVariable("Cisco-id") Long ciscoId) {
        ciscoService.deleteById(ciscoId);
    }

    @PutMapping("/approuve/{id}")
    public void activateCisco(@PathVariable("id") Long id) {
        ciscoService.activate(id);
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

            Cisco cisco = ciscoService.retrieveCisco(id);
            if (cisco == null) {
                response.put("success", false);
                response.put("message", "Cisco non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Supprimer l'ancien fichier s'il existe
            if (cisco.getFichier() != null && !cisco.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = uploadPath.resolve(cisco.getFichier());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.out.println("Impossible de supprimer l'ancien fichier: " + e.getMessage());
                }
            }

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(uniqueFilename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Cisco updatedCisco = ciscoService.updateCiscoFile(id, uniqueFilename, originalFilename);

            response.put("success", true);
            response.put("message", "Fichier uploadé avec succès");
            response.put("data", updatedCisco);
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
            Cisco cisco = ciscoService.retrieveCisco(id);
            if (cisco == null || cisco.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(FILE_DIRECTORY).resolve(cisco.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "application/octet-stream";
                String originalFileName = cisco.getFichierOriginalName() != null ? 
                    cisco.getFichierOriginalName() : cisco.getFichier();

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
            Cisco cisco = ciscoService.retrieveCisco(id);
            if (cisco == null || cisco.getFichier() == null) {
                response.put("success", false);
                response.put("message", "Cisco ou fichier non trouvé");
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(FILE_DIRECTORY).resolve(cisco.getFichier()).normalize();
            Files.deleteIfExists(filePath);

            Cisco updatedCisco = ciscoService.updateCiscoFile(id, null, null);

            response.put("success", true);
            response.put("message", "Fichier supprimé avec succès");
            response.put("data", updatedCisco);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
