package com.example.projet2024.controller;

import com.example.projet2024.entite.Alwarebytes;
import com.example.projet2024.entite.ESET;
import com.example.projet2024.entite.LicenceFortinet;
import com.example.projet2024.service.IAlwarebytesService;
import com.example.projet2024.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/Alwarebytes")
@CrossOrigin(origins = "http://localhost:4200")
public class AlwarebytesController {
    @Autowired
    private IAlwarebytesService alwarebytesService;
    @Autowired
    private EmailService emailService;

    private final String FILE_DIRECTORY = "uploads/alwarebytes-files/";

    @PostMapping("/addAlwarebytes")
    public Alwarebytes addAlwarebytes(@RequestBody Alwarebytes alwarebytes){
        return alwarebytesService.addAlwarebytes(alwarebytes);
    }

    @PutMapping("/updateAlwarebytes")
    public Alwarebytes updateAlwarebytes(@RequestBody Alwarebytes alwarebytes) {
        return alwarebytesService.updateAlwarebytes(alwarebytes);
    }

    @PutMapping("/{id}/upload-file")
    public Alwarebytes uploadFile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) throws IOException {
        File dir = new File(FILE_DIRECTORY);
        if (!dir.exists()) dir.mkdirs();
        String originalName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String newFileName = uuid + "_" + originalName;
        Path filePath = Paths.get(FILE_DIRECTORY, newFileName);
        Files.write(filePath, file.getBytes());
        return alwarebytesService.updateAlwarebytesFile(id, newFileName, originalName);
    }

    @GetMapping("/{id}/download")
    public @ResponseBody byte[] downloadFile(@PathVariable("id") Long id) throws IOException {
        Alwarebytes alwarebytes = alwarebytesService.retrieveAlwarebytes(id);
        if (alwarebytes != null && alwarebytes.getFichier() != null) {
            Path filePath = Paths.get(FILE_DIRECTORY, alwarebytes.getFichier());
            return Files.readAllBytes(filePath);
        }
        return null;
    }

    @DeleteMapping("/{id}/delete-file")
    public Alwarebytes deleteFile(@PathVariable("id") Long id) throws IOException {
        Alwarebytes alwarebytes = alwarebytesService.retrieveAlwarebytes(id);
        if (alwarebytes != null && alwarebytes.getFichier() != null) {
            Path filePath = Paths.get(FILE_DIRECTORY, alwarebytes.getFichier());
            Files.deleteIfExists(filePath);
            return alwarebytesService.updateAlwarebytesFile(id, null, null);
        }
        return alwarebytes;
    }

    @GetMapping("/get/{id-Alwarebytes}")
    public Alwarebytes getById(@PathVariable("id-Alwarebytes") Long alwarebytesId) {
        return alwarebytesService.retrieveAlwarebytes(alwarebytesId);
    }

    @GetMapping("/allAlwarebytes")
    public List<Alwarebytes> getAllAlwarebytess() {
        return alwarebytesService.retrieveAllAlwarebytess();
    }

    @DeleteMapping("/delete/{Alwarebytes-id}")
    public void deleteById(@PathVariable("Alwarebytes-id") Long alwarebytesId) {
        alwarebytesService.deleteById(alwarebytesId);
    }

    @PutMapping("/approuve/{id}")
    public void activateAlwarebytes(@PathVariable("id") Long id) {
        alwarebytesService.activate(id);
    }
}
