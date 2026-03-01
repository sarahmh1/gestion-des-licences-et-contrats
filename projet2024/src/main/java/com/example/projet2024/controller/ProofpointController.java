package com.example.projet2024.controller;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.fortinet.Fortinet;
import com.example.projet2024.entite.proofpoint.Proofpoint;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.ProofpointServiceImpl;
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
@RequestMapping("/Proofpoint")
@CrossOrigin(origins = "http://localhost:4200")
public class ProofpointController {

    @Autowired
    private ProofpointServiceImpl proofpointService;
    @Autowired
    private EmailService emailService;

    private final Path fileStorageLocation = Paths.get("uploads/proofpoint-files").toAbsolutePath().normalize();

    public ProofpointController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory for file uploads.", ex);
        }
    }

    @PostMapping("/addProofpoint")
    public Proofpoint addProofpoint(@RequestBody Proofpoint proofpoint) {
        return proofpointService.addProofpoint(proofpoint);
    }

    @PutMapping("/updateProofpoint")
    public Proofpoint updateProofpoint(@RequestBody Proofpoint proofpoint) {
        return proofpointService.updateProofpoint(proofpoint);
    }

    @GetMapping("/get/{id-Proofpoint}")
    public Proofpoint getById(@PathVariable("id-Proofpoint") Long proofpointId) {
        return proofpointService.retrieveProofpoint(proofpointId);
    }

    @GetMapping("/allProofpoint")
    public List<Proofpoint> getAllProofpoints() {
        return proofpointService.retrieveAllProofpoints();
    }

    @DeleteMapping("/delete/{Proofpoint-id}")
    public void deleteById(@PathVariable("Proofpoint-id") Long proofpointId) {
        proofpointService.deleteById(proofpointId);
    }

    @PutMapping("/approuve/{id}")
    public void activateProofpoint(@PathVariable("id") Long id) {
        proofpointService.activate(id);
    }

    // Upload file for Proofpoint
    @PostMapping("/{id}/upload-file")
    public ResponseEntity<?> uploadFile(@PathVariable("id") Long proofpointId, @RequestParam("file") MultipartFile file) {
        try {
            Proofpoint proofpoint = proofpointService.retrieveProofpoint(proofpointId);
            if (proofpoint == null) {
                return ResponseEntity.notFound().build();
            }

            // Delete old file if exists
            if (proofpoint.getFichier() != null && !proofpoint.getFichier().isEmpty()) {
                try {
                    Path oldFilePath = this.fileStorageLocation.resolve(proofpoint.getFichier()).normalize();
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.err.println("Could not delete old file: " + e.getMessage());
                }
            }

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Update Proofpoint with file info (using updateProofpointFile to preserve licences)
            Proofpoint updatedProofpoint = proofpointService.updateProofpointFile(proofpointId, newFileName, originalFileName);

            return ResponseEntity.ok(updatedProofpoint);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("Could not upload file: " + ex.getMessage());
        }
    }

    // Download file for Proofpoint
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long proofpointId) {
        try {
            Proofpoint proofpoint = proofpointService.retrieveProofpoint(proofpointId);
            if (proofpoint == null || proofpoint.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(proofpoint.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "application/octet-stream";
                String originalFileName = proofpoint.getFichierOriginalName() != null ? proofpoint.getFichierOriginalName() : proofpoint.getFichier();

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete file for Proofpoint
    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<?> deleteFile(@PathVariable("id") Long proofpointId) {
        try {
            Proofpoint proofpoint = proofpointService.retrieveProofpoint(proofpointId);
            if (proofpoint == null) {
                return ResponseEntity.notFound().build();
            }

            if (proofpoint.getFichier() != null && !proofpoint.getFichier().isEmpty()) {
                Path filePath = this.fileStorageLocation.resolve(proofpoint.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            // Update Proofpoint with null file info (using updateProofpointFile to preserve licences)
            Proofpoint updatedProofpoint = proofpointService.updateProofpointFile(proofpointId, null, null);

            return ResponseEntity.ok(updatedProofpoint);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("Could not delete file: " + ex.getMessage());
        }
    }
}
