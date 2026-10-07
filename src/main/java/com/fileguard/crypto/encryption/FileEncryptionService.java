package com.fileguard.crypto.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class FileEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int KEY_SIZE = 256;

    private final Path encryptedDirectory;

    public FileEncryptionService(
            @Value("${fileguard.storage.encrypted-dir}")
            String encryptedDirectory) {

        this.encryptedDirectory = Path.of(encryptedDirectory);

        try {
            Files.createDirectories(this.encryptedDirectory);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to create encrypted storage directory", e
            );
        }
    }

    public EncryptionResult encryptFile(
            Path inputFile,
            String outputFileName) throws Exception {

        byte[] fileData = Files.readAllBytes(inputFile);

        KeyGenerator keyGenerator =
                KeyGenerator.getInstance("AES");

        keyGenerator.init(KEY_SIZE);

        SecretKey secretKey = keyGenerator.generateKey();

        byte[] iv = new byte[IV_LENGTH];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);

        GCMParameterSpec gcmSpec =
                new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(
                Cipher.ENCRYPT_MODE,
                secretKey,
                gcmSpec
        );

        byte[] encryptedData =
                cipher.doFinal(fileData);

        Path outputFile =
                encryptedDirectory.resolve(outputFileName);

        Files.write(
                outputFile,
                encryptedData,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        return new EncryptionResult(
                outputFile.toString(),
                Base64.getEncoder().encodeToString(secretKey.getEncoded()),
                Base64.getEncoder().encodeToString(iv)
        );
    }

    public byte[] decryptFile(
            Path encryptedFile,
            String encodedKey,
            String encodedIv) throws Exception {

        byte[] encryptedData =
                Files.readAllBytes(encryptedFile);

        byte[] keyBytes =
                Base64.getDecoder().decode(encodedKey);

        byte[] iv =
                Base64.getDecoder().decode(encodedIv);

        SecretKey secretKey =
                new javax.crypto.spec.SecretKeySpec(
                        keyBytes,
                        "AES"
                );

        Cipher cipher =
                Cipher.getInstance(ALGORITHM);

        GCMParameterSpec gcmSpec =
                new GCMParameterSpec(
                        GCM_TAG_LENGTH,
                        iv
                );

        cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                gcmSpec
        );

        return cipher.doFinal(encryptedData);
    }

    public record EncryptionResult(
            String encryptedFilePath,
            String encryptionKey,
            String initializationVector
    ) {
    }
}
