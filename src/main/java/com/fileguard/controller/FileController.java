package com.fileguard.controller;

import com.fileguard.crypto.encryption.FileEncryptionService;
import com.fileguard.crypto.encryption.FileEncryptionService.EncryptionResult;
import com.fileguard.risk.decision.RiskAnalysisService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    private final FileEncryptionService encryptionService;
    private final RiskAnalysisService riskAnalysisService;
    private final Path uploadDirectory;

    public FileController(
            FileEncryptionService encryptionService,
            RiskAnalysisService riskAnalysisService,
            @Value("${fileguard.storage.upload-dir}") String uploadDirectory) {

        this.encryptionService = encryptionService;
        this.riskAnalysisService = riskAnalysisService;
        this.uploadDirectory = Path.of(uploadDirectory);

        try {
            Files.createDirectories(this.uploadDirectory);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to create upload directory", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File is empty"));
            }

            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null ||
                    originalFileName.isBlank()) {

                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid file name"));
            }

            byte[] fileContent = file.getBytes();

            Map<String, Object> riskAnalysis =
                    riskAnalysisService.analyze(
                            originalFileName,
                            file.getSize(),
                            fileContent
                    );

            String safeFileName =
                    UUID.randomUUID() + "_" +
                    Path.of(originalFileName)
                            .getFileName()
                            .toString();

            Path uploadedFile =
                    uploadDirectory.resolve(safeFileName);

            Files.write(uploadedFile, fileContent);

            String encryptedFileName =
                    safeFileName + ".enc";

            EncryptionResult encryptionResult =
                    encryptionService.encryptFile(
                            uploadedFile,
                            encryptedFileName
                    );

            Files.deleteIfExists(uploadedFile);

            Map<String, Object> response = new HashMap<>();

            response.put(
                    "message",
                    "File analyzed and encrypted successfully"
            );

            response.put(
                    "originalFileName",
                    originalFileName
            );

            response.put(
                    "fileSize",
                    file.getSize()
            );

            response.put(
                    "contentType",
                    file.getContentType()
            );

            response.put(
                    "riskAnalysis",
                    riskAnalysis
            );

            response.put(
                    "encryptedFile",
                    encryptionResult.encryptedFilePath()
            );

            response.put(
                    "encryptionKey",
                    encryptionResult.encryptionKey()
            );

            response.put(
                    "initializationVector",
                    encryptionResult.initializationVector()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "File processing failed",
                            "message", e.getMessage()
                    ));
        }
    }
}
