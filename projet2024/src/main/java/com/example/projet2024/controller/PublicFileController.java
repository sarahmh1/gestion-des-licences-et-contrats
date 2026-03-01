package com.example.projet2024.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:4200")
public class PublicFileController {

    @GetMapping("/profiles/{filename:.+}")
    public ResponseEntity<FileSystemResource> getProfileImage(@PathVariable String filename) {
        try {
            // ✅ Chemin ABSOLU - utilisez le chemin complet de votre projet
            String absolutePath = "C:/Users/Sarra/Downloads/projet4éme/projet2024/uploads/profiles/" + filename;
            File file = new File(absolutePath);

            System.out.println("🔍 Looking for file: " + file.getAbsolutePath());
            System.out.println("📁 File exists: " + file.exists());
            System.out.println("📁 File readable: " + file.canRead());

            if (file.exists() && file.canRead()) {
                System.out.println("✅ Serving file: " + filename);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new FileSystemResource(file));
            } else {
                System.out.println("❌ File not found: " + filename);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}