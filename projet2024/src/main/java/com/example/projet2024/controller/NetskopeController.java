package com.example.projet2024.controller;

import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.entite.Netskope;
import com.example.projet2024.entite.vmware.VMware;
import com.example.projet2024.service.EmailService;
import com.example.projet2024.service.NetskopeServiceImpl;
import com.example.projet2024.service.VMwareServiceImpl;
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
@RequestMapping("/Netskope")
@CrossOrigin(origins = "http://localhost:4200")
public class NetskopeController {
    @Autowired
    private NetskopeServiceImpl netskopeService;
    @Autowired
    private EmailService emailService;

    private final Path fileStorageLocation = Paths.get("uploads/netskope-files").toAbsolutePath().normalize();

    public NetskopeController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory for file uploads.", ex);
        }
    }
    @PostMapping("/addNetskope")
    public Netskope addNetskope(@RequestBody Netskope netskope) {
        return  netskopeService.addNetskope(netskope);
    }

    @PutMapping("/updateNetskope")
    public Netskope updateNetskope(@RequestBody Netskope netskope) {
        return netskopeService.updateNetskope(netskope);
    }

    @GetMapping("/get/{id-Netskope}")
    public Netskope getById(@PathVariable("id-Netskope") Long netskopeId) {
        return netskopeService.retrieveNetskope(netskopeId);
    }

    @GetMapping("/allNetskope")
    public List<Netskope> getAllNetskopes() {
        return netskopeService.retrieveAllNetskopes();
    }

    @DeleteMapping("/delete/{Netskope-id}")
    public void deleteById(@PathVariable("Netskope-id") Long netskopeId) {
        netskopeService.deleteById(netskopeId);
    }

    @PutMapping("/approuve/{id}")
    public void activateNetskope(@PathVariable("id") Long id) {
        netskopeService.activate(id);
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<Netskope> uploadFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            Netskope netskope = netskopeService.retrieveNetskope(id);
            if (netskope == null) {
                return ResponseEntity.notFound().build();
            }

            // Supprimer l'ancien fichier s'il existe
            if (netskope.getFichier() != null) {
                Path oldFilePath = this.fileStorageLocation.resolve(netskope.getFichier()).normalize();
                Files.deleteIfExists(oldFilePath);
            }

            // Générer un nom unique pour le fichier
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Sauvegarder le fichier
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Mettre à jour l'entité
            Netskope updated = netskopeService.updateNetskopeFile(id, newFileName, originalFileName);
            return ResponseEntity.ok(updated);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) {
        try {
            Netskope netskope = netskopeService.retrieveNetskope(id);
            if (netskope == null || netskope.getFichier() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = this.fileStorageLocation.resolve(netskope.getFichier()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + netskope.getFichierOriginalName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/delete-file")
    public ResponseEntity<Netskope> deleteFile(@PathVariable("id") Long id) {
        try {
            Netskope netskope = netskopeService.retrieveNetskope(id);
            if (netskope == null) {
                return ResponseEntity.notFound().build();
            }

            // Supprimer le fichier physique
            if (netskope.getFichier() != null) {
                Path filePath = this.fileStorageLocation.resolve(netskope.getFichier()).normalize();
                Files.deleteIfExists(filePath);
            }

            // Mettre à jour l'entité
            Netskope updated = netskopeService.updateNetskopeFile(id, null, null);
            return ResponseEntity.ok(updated);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
