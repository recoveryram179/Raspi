package com.hawks.raspi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(file.getOriginalFilename());

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println(filePath);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
