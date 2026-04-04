package com.veritas.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Catches file-too-large errors.
     * Spring throws this BEFORE the controller is called,
     * which is why you see an empty page instead of an error message.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSize(MaxUploadSizeExceededException ex, Model model) {
        model.addAttribute("videoError",
                "File too large. Maximum allowed size is 500 MB. Try a shorter clip.");
        return "index";
    }

    /**
     * Catches any other unexpected crash and shows it on the page
     * instead of returning an empty response.
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        model.addAttribute("videoError",
                "Server error: " + ex.getClass().getSimpleName() + " — " + ex.getMessage());
        return "index";
    }
}