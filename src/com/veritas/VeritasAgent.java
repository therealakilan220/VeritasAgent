package com.veritas;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import java.time.Duration;

public class VeritasAgent {
    public static void main(String[] args) {

        // 1. Setup the Connection to your local Ollama engine
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3.2")
                .timeout(Duration.ofSeconds(60))
                .build();

        // 2. Create the AI Agent (The Crew)
        VeritasCrew myAgent = AiServices.create(VeritasCrew.class, model);

        // 3. Run the analysis inside a Java 25 Virtual Thread
        Thread.startVirtualThread(() -> {
            System.out.println("[SYSTEM] Analysis started on Virtual Thread...");

            String testInput = "A video of a politician speaking, but the shadows on their neck don't match the overhead sun.";
            String report = myAgent.analyzeMedia(testInput);

            System.out.println("\n--- FINAL FORENSIC REPORT ---");
            System.out.println(report);
            System.out.println("----------------------------");
        });

        // Keep the main thread alive long enough for the virtual thread to finish
        try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
    }
}