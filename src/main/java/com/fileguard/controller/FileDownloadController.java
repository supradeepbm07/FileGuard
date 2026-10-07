package com.fileguard.controller;

import com.fileguard.crypto.encryption.FileEncryptionService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/files")
public class FileDownloadController {

    private final FileEncryptionService encryptionService;

    public FileDownloadController(
            FileEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @GetMapping("/decrypt")
    public ResponseEntity<?> decryptFile(
            @RequestParam String encryptedFile,
            @RequestParam String key,
            @RequestParam String iv,
            @RequestParam(defaultValue = "decrypted-file") String fileName) {

        try {
            Path encryptedPath = Path.of(encryptedFile);

            if (!Files.exists(encryptedPath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] decryptedData =
                    encryptionService.decryptFile(
                            encryptedPath,
                            key,
                            iv
                    );

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\""
                    )
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(decryptedData);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            java.util.Map.of(
                                    "error",
                                    "Unable to decrypt file",
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}