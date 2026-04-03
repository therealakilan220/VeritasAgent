package com.veritas;

import org.springframework.web.bind.annotation.*;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

@RestController
@RequestMapping("/api/forensics")
public class ForensicController {

    @PostMapping("/analyze")
    public String analyze(@RequestBody String mediaDescription) {
        // 1. Setup the Model
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3.2")
                .build();

        // 2. Create the Agent
        VeritasCrew crew = AiServices.create(VeritasCrew.class, model);

        // 3. Return the AI's forensic report
        return crew.analyzeMedia(mediaDescription);
    }
}