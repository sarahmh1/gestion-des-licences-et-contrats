package com.example.projet2024.controller;

import com.example.projet2024.entite.Infoblox;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.IInfobloxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/Infoblox")
@CrossOrigin(origins = "http://localhost:4200")
public class InfobloxController {
    
    private static final String FILE_DIRECTORY = "uploads/infoblox-files/";
    
    @Autowired
    private IInfobloxService infobloxService;
    
    @Autowired
    private EmailService emailService;

    @PostMapping("/addInfoblox")
    public Infoblox addInfoblox(@RequestBody Infoblox infoblox) {
        return infobloxService.addInfoblox(infoblox);
    }

    @PutMapping("/updateInfoblox")
    public Infoblox updateInfoblox(@RequestBody Infoblox infoblox) {
        return infobloxService.updateInfoblox(infoblox);
    }

    @GetMapping("/get/{id-Infoblox}")
    public Infoblox getById(@PathVariable("id-Infoblox") Long infobloxId) {
        return infobloxService.retrieveInfoblox(infobloxId);
    }

    @GetMapping("/allInfoblox")
    public List<Infoblox> getAllInfobloxs() {
        return infobloxService.retrieveAllInfobloxs();
    }

    @DeleteMapping("/delete/{Infoblox-id}")
    public void deleteById(@PathVariable("Infoblox-id") Long infobloxId) {
        infobloxService.deleteById(infobloxId);
    }

    @PutMapping("/approuve/{id}")
    public void activateInfoblox(@PathVariable("id") Long id) {
        infobloxService.activate(id);
    }

    // ==================== GESTION DES FICHIERS ====================

    @PutMapping("/{id}/upload-file")
    public ResponseEntity<Infoblox> uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            File dir = new File(FILE_DIRECTORY);
            if (!dir.exists()) dir.mkdirs();

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = Paths.get(FILE_DIRECTORY, uniqueFilename);

            Files.write(filePath, file.getBytes());

            infobloxService.updateInfobloxFile(id, uniqueFilename, originalFilename);

            Infoblox updatedInfoblox = infobloxService.retrieveInfoblox(id);
            return ResponseEntity.ok(updatedInfoblox);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            Infoblox infoblox = infobloxService.retrieveInfoblox(id);
            if (infoblox == null || infoblox.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(FILE_DIRECTORY).resolve(infoblox.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                String downloadFilename = infoblox.getFichierOriginalName() != null ? 
                        infoblox.getFichierOriginalName() : resource.getFilename();

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFilename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<Infoblox> deleteFile(@PathVariable Long id) {
        try {
            Infoblox infoblox = infobloxService.retrieveInfoblox(id);
            if (infoblox == null || infoblox.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(FILE_DIRECTORY).resolve(infoblox.getFichier());
            Files.deleteIfExists(filePath);

            infobloxService.updateInfobloxFile(id, null, null);

            return ResponseEntity.ok(infobloxService.retrieveInfoblox(id));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
