package com.veritas.model;

import java.util.List;

public class VideoResult {

    private final String filename;
    private final List<CheckResult> signals;
    private final int confidencePercent;  // 0–100: confidence that it IS a deepfake
    private final int framesScanned;
    private final int anomalyCount;

    public VideoResult(String filename, List<CheckResult> signals,
                       int confidencePercent, int framesScanned, int anomalyCount) {
        this.filename         = filename;
        this.signals          = signals;
        this.confidencePercent = confidencePercent;
        this.framesScanned    = framesScanned;
        this.anomalyCount     = anomalyCount;
    }

    public String getFilename()          { return filename; }
    public List<CheckResult> getSignals(){ return signals; }
    public int getConfidencePercent()    { return confidencePercent; }
    public int getFramesScanned()        { return framesScanned; }
    public int getAnomalyCount()         { return anomalyCount; }

    /** "safe" if deepfake confidence is low, "danger" if high */
    public String getDeepfakeClass() {
        return confidencePercent >= 60 ? "danger" : "safe";
    }

    public String getVerdictTitle() {
        return confidencePercent >= 60 ? "Deepfake Detected" : "Appears Authentic";
    }

    public String getVerdictMessage() {
        if (confidencePercent >= 60) {
            return confidencePercent + "% confidence of AI-generated manipulation. Do not share.";
        }
        return "No significant manipulation artifacts detected (" + confidencePercent + "% deepfake confidence).";
    }
}
