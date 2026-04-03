package com.veritas.controller;

import com.veritas.model.AnalysisResult;
import com.veritas.model.VideoResult;
import com.veritas.service.UrlAnalyzerService;
import com.veritas.service.VideoAnalyzerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class HomeController {

    private final UrlAnalyzerService   urlAnalyzerService;
    private final VideoAnalyzerService videoAnalyzerService;

    public HomeController(UrlAnalyzerService urlAnalyzerService,
                          VideoAnalyzerService videoAnalyzerService) {
        this.urlAnalyzerService   = urlAnalyzerService;
        this.videoAnalyzerService = videoAnalyzerService;
    }

    /** Landing page */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // URL / Claim analysis  →  POST /analyze
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/analyze")
    public String analyzeUrl(@RequestParam(required = false) String url, Model model) {

        if (url == null || url.isBlank()) {
            model.addAttribute("error", "Please enter a URL or claim to analyze.");
            return "index";
        }

        String trimmed = url.trim();

        // Auto-prepend https:// if no scheme present so URI parsing works
        String normalised = trimmed;
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            normalised = "https://" + trimmed;
        }

        try {
            AnalysisResult result = urlAnalyzerService.analyze(normalised);
            model.addAttribute("result", result);
            model.addAttribute("url", trimmed);
        } catch (Exception e) {
            model.addAttribute("error", "Analysis failed: " + e.getMessage());
        }

        return "index";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Video / deepfake analysis  →  POST /analyze/video
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/analyze/video")
    public String analyzeVideo(@RequestParam(value = "video", required = false) MultipartFile file,
                               Model model) {

        if (file == null || file.isEmpty()) {
            model.addAttribute("videoError", "Please upload a video file to analyze.");
            return "index";
        }

        // Basic size guard: 500 MB max
        if (file.getSize() > 500L * 1024 * 1024) {
            model.addAttribute("videoError", "File too large. Maximum supported size is 500 MB.");
            return "index";
        }

        try {
            VideoResult videoResult = videoAnalyzerService.analyze(file);
            model.addAttribute("videoResult", videoResult);
        } catch (Exception e) {
            model.addAttribute("videoError", "Video analysis failed: " + e.getMessage());
        }

        return "index";
    }
}
