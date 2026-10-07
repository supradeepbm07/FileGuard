package com.fileguard.risk.decision;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class RiskAnalysisService {

    private final Tika tika = new Tika();

    private static final Set<String> HIGH_RISK_EXTENSIONS = Set.of(
            "exe", "dll", "bat", "cmd", "scr",
            "ps1", "vbs", "js", "jar", "msi", "com"
    );

    private static final Set<String> SUSPICIOUS_EXTENSIONS = Set.of(
            "zip", "rar", "7z", "iso", "img"
    );

    public Map<String, Object> analyze(
            String fileName,
            long fileSize,
            byte[] fileContent) {

        Map<String, Object> result = new HashMap<>();

        String extension = getExtension(fileName);
        String detectedType = tika.detect(fileContent);

        int riskScore = 0;

        if (HIGH_RISK_EXTENSIONS.contains(extension)) {
            riskScore += 70;
        } else if (SUSPICIOUS_EXTENSIONS.contains(extension)) {
            riskScore += 30;
        }

        if (fileSize > 50 * 1024 * 1024) {
            riskScore += 10;
        }

        if (detectedType == null ||
                detectedType.equals("application/octet-stream")) {
            riskScore += 10;
        }

        riskScore = Math.min(riskScore, 100);

        String riskLevel;

        if (riskScore >= 70) {
            riskLevel = "HIGH";
        } else if (riskScore >= 30) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        result.put("fileName", fileName);
        result.put("fileSize", fileSize);
        result.put("extension", extension);
        result.put("detectedMimeType", detectedType);
        result.put("riskScore", riskScore);
        result.put("riskLevel", riskLevel);

        return result;
    }

    private String getExtension(String fileName) {

        int lastDot = fileName.lastIndexOf('.');

        if (lastDot == -1 ||
                lastDot == fileName.length() - 1) {
            return "";
        }

        return fileName
                .substring(lastDot + 1)
                .toLowerCase();
    }
}
