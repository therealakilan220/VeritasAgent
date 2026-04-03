package com.veritas.model;

import java.util.List;

public class AnalysisResult {

    private final String url;
    private final List<CheckResult> checks;
    private final int riskScore;
    private final int maxScore;

    public AnalysisResult(String url, List<CheckResult> checks, int riskScore, int maxScore) {
        this.url = url;
        this.checks = checks;
        this.riskScore = riskScore;
        this.maxScore = maxScore;
    }

    public String getUrl() { return url; }
    public List<CheckResult> getChecks() { return checks; }
    public int getRiskScore() { return riskScore; }
    public int getMaxScore() { return maxScore; }

    public int getThreatPercent() {
        if (maxScore == 0) return 0;
        return (riskScore * 100) / maxScore;
    }

    public String getThreatLevel() {
        int pct = getThreatPercent();
        if (pct <= 20) return "LOW";
        if (pct <= 55) return "MEDIUM";
        return "HIGH";
    }

    public String getThreatClass() {
        return switch (getThreatLevel()) {
            case "LOW"    -> "safe";
            case "MEDIUM" -> "warn";
            default       -> "danger";
        };
    }

    public String getVerdictTitle() {
        return switch (getThreatLevel()) {
            case "LOW"    -> "Safe Website";
            case "MEDIUM" -> "Suspicious Website";
            default       -> "High-Risk Phishing URL";
        };
    }

    public String getVerdictMessage() {
        return switch (getThreatLevel()) {
            case "LOW"    -> "No significant threats detected. This URL appears safe.";
            case "MEDIUM" -> "Some risk indicators found. Proceed with caution.";
            default       -> "Multiple threat indicators detected. Do not visit this URL.";
        };
    }

    public long getPassCount() {
        return checks.stream().filter(CheckResult::isPass).count();
    }

    public long getWarnCount() {
        return checks.stream().filter(CheckResult::isWarn).count();
    }

    public long getFailCount() {
        return checks.stream().filter(CheckResult::isFail).count();
    }
}