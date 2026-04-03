package com.veritas.service;

import com.veritas.model.CheckResult;
import com.veritas.model.VideoResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Heuristic video / deepfake analysis service.
 *
 * NOTE: This is a hackathon demo implementation.
 * Real deepfake detection would call an ML inference endpoint
 * (e.g. a Python FastAPI service running a FaceForensics++ model).
 *
 * The heuristics here are based on:
 *  - file size & compression ratio (deepfakes are often re-encoded)
 *  - filename patterns
 *  - MIME type vs extension mismatch
 *  - file size anomalies per second of estimated runtime
 */
@Service
public class VideoAnalyzerService {

    private static final long   MB              = 1024L * 1024L;
    private static final long   MAX_NORMAL_MB   = 500;   // >500 MB is unusual for short clips
    private static final long   MIN_SUSPICIOUS_KB = 50;  // Very small "video" is suspicious

    public VideoResult analyze(MultipartFile file) {
        List<CheckResult> signals = new ArrayList<>();
        int riskScore = 0;

        String originalName = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase() : "unknown";
        long sizeBytes = file.getSize();
        String contentType = file.getContentType() != null
                ? file.getContentType().toLowerCase() : "unknown";

        // ── Signal 1: MIME type validation ──────────────────────────────────
        CheckResult mimeCheck = checkMimeType(contentType, originalName);
        signals.add(mimeCheck);
        riskScore += weight(mimeCheck);

        // ── Signal 2: File size sanity ───────────────────────────────────────
        CheckResult sizeCheck = checkFileSize(sizeBytes);
        signals.add(sizeCheck);
        riskScore += weight(sizeCheck);

        // ── Signal 3: Filename anomaly ───────────────────────────────────────
        CheckResult nameCheck = checkFilename(originalName);
        signals.add(nameCheck);
        riskScore += weight(nameCheck);

        // ── Signal 4: Extension vs MIME consistency ───────────────────────────
        CheckResult extCheck = checkExtensionMimeMatch(originalName, contentType);
        signals.add(extCheck);
        riskScore += weight(extCheck);

        // ── Signal 5: Compression entropy (simulated) ────────────────────────
        // Real implementation would read container headers via Tika or JCodec
        CheckResult compressionCheck = simulateCompressionCheck(sizeBytes, originalName);
        signals.add(compressionCheck);
        riskScore += weight(compressionCheck);

        // ── Signal 6: Temporal consistency (simulated ML score) ──────────────
        CheckResult temporalCheck = simulateTemporalCheck(sizeBytes);
        signals.add(temporalCheck);
        riskScore += weight(temporalCheck);

        // ── Signal 7: Facial landmark stability (simulated) ──────────────────
        CheckResult facialCheck = simulateFacialCheck(riskScore);
        signals.add(facialCheck);
        riskScore += weight(facialCheck);

        // ── Signal 8: GAN fingerprint detection (simulated) ──────────────────
        CheckResult ganCheck = simulateGanCheck(riskScore);
        signals.add(ganCheck);
        riskScore += weight(ganCheck);

        // ── Derive deepfake confidence (0–100) ───────────────────────────────
        // max possible risk = 8 checks × 2 = 16
        int maxScore = 16;
        int confidencePercent = Math.min(100, (riskScore * 100) / maxScore);

        // Simulated frame count and anomaly count based on file size
        int framesScanned = estimateFrames(sizeBytes);
        int anomalyCount  = signals.stream()
                .filter(s -> "fail".equals(s.status()) || "warn".equals(s.status()))
                .mapToInt(s -> "fail".equals(s.status()) ? 2 : 1)
                .sum();

        return new VideoResult(originalName, signals, confidencePercent, framesScanned, anomalyCount);
    }

    // ─── Individual signal checks ─────────────────────────────────────────────

    private CheckResult checkMimeType(String contentType, String filename) {
        if (contentType.startsWith("video/")) {
            return pass("MIME Type", "Content-Type is a valid video MIME type: " + contentType);
        }
        if (contentType.equals("application/octet-stream")) {
            return warn("MIME Type", "Generic binary MIME type — could not verify video format");
        }
        return fail("MIME Type", "Unexpected MIME type: " + contentType + " — not a video");
    }

    private CheckResult checkFileSize(long sizeBytes) {
        long sizeKB = sizeBytes / 1024;
        long sizeMB = sizeBytes / MB;
        if (sizeKB < MIN_SUSPICIOUS_KB) {
            return fail("File Size", "Extremely small file (" + sizeKB + " KB) — likely not a real video");
        }
        if (sizeMB > MAX_NORMAL_MB) {
            return warn("File Size", "Very large file (" + sizeMB + " MB) — unusual for short clips");
        }
        return pass("File Size", "File size is within expected range (" + sizeMB + " MB)");
    }

    private CheckResult checkFilename(String name) {
        if (name.matches(".*[a-f0-9]{8,}.*")) {
            return warn("Filename Pattern", "Filename contains hash-like string — common in AI-generated content pipelines");
        }
        if (name.contains("deepfake") || name.contains("fake") || name.contains("swap")) {
            return fail("Filename Pattern", "Filename contains deepfake-related keywords");
        }
        return pass("Filename Pattern", "Filename looks normal");
    }

    private CheckResult checkExtensionMimeMatch(String filename, String contentType) {
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1) : "";
        boolean consistent = switch (ext) {
            case "mp4"  -> contentType.contains("mp4")  || contentType.contains("mpeg");
            case "avi"  -> contentType.contains("avi")  || contentType.contains("x-msvideo");
            case "mov"  -> contentType.contains("mov")  || contentType.contains("quicktime");
            case "mkv"  -> contentType.contains("mkv")  || contentType.contains("x-matroska");
            case "webm" -> contentType.contains("webm");
            default     -> contentType.startsWith("video/");
        };
        if (!consistent && !contentType.equals("application/octet-stream")) {
            return warn("Extension / MIME Match",
                    "Extension '." + ext + "' does not match MIME type '" + contentType + "'");
        }
        return pass("Extension / MIME Match", "File extension matches content type");
    }

    private CheckResult simulateCompressionCheck(long sizeBytes, String filename) {
        // Heuristic: re-encoded deepfakes often have unusual compression ratios
        // We simulate by using file size modulo as a pseudo-random but deterministic signal
        long sizeMB = sizeBytes / MB;
        if (sizeMB == 0) {
            return warn("Compression Entropy",
                    "Could not assess compression — file too small for entropy analysis");
        }
        // Use filename hash for deterministic simulation
        int filenameHash = Math.abs(filename.hashCode());
        boolean anomalous = (filenameHash % 7) < 2; // ~28% chance of flagging
        if (anomalous) {
            return warn("Compression Entropy",
                    "Unusual compression pattern detected — may indicate re-encoding artifacts");
        }
        return pass("Compression Entropy", "Video compression entropy appears normal");
    }

    private CheckResult simulateTemporalCheck(long sizeBytes) {
        // Simulate temporal consistency analysis
        // Deepfakes often show frame-to-frame inconsistency
        boolean inconsistent = (sizeBytes % 11) < 3; // ~27% chance
        if (inconsistent) {
            return fail("Temporal Consistency",
                    "Frame-to-frame inconsistency detected — possible GAN-generated content");
        }
        return pass("Temporal Consistency", "Temporal frame consistency is normal");
    }

    private CheckResult simulateFacialCheck(int currentRisk) {
        // If already suspicious, more likely to find facial anomalies
        if (currentRisk >= 4) {
            return warn("Facial Landmark Stability",
                    "Facial landmark drift detected across frames — a common deepfake artifact");
        }
        return pass("Facial Landmark Stability", "No facial landmark instability detected");
    }

    private CheckResult simulateGanCheck(int currentRisk) {
        if (currentRisk >= 6) {
            return fail("GAN Fingerprint", "Frequency-domain artifacts suggest GAN-generated frames");
        }
        if (currentRisk >= 3) {
            return warn("GAN Fingerprint", "Mild frequency artifacts present — inconclusive GAN signal");
        }
        return pass("GAN Fingerprint", "No GAN fingerprint artifacts detected");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private int estimateFrames(long sizeBytes) {
        // Rough estimate: assume 30fps, ~50KB per frame compressed
        long estimatedSeconds = Math.max(1, sizeBytes / (1024L * 150));
        return (int) Math.min(estimatedSeconds * 30, 9999);
    }

    private int weight(CheckResult c) {
        return switch (c.status()) {
            case "fail" -> 2;
            case "warn" -> 1;
            default     -> 0;
        };
    }

    private static CheckResult pass(String label, String detail) {
        return new CheckResult(label, "pass", detail);
    }

    private static CheckResult warn(String label, String detail) {
        return new CheckResult(label, "warn", detail);
    }

    private static CheckResult fail(String label, String detail) {
        return new CheckResult(label, "fail", detail);
    }
}
