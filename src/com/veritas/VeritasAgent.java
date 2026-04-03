package com.veritas;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class VeritasAgent {
    public static void main(String[] args) {
        String prompt = "Analyze this: Video shows inconsistent shadows on deepfake media.";

        // JSON for Ollama API
        String json = "{\"model\": \"llama3.2\", \"prompt\": \"" + prompt + "\", \"stream\": false}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        System.out.println("[SYSTEM] Sending request to local Ollama...");

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println)
                .join();
    }
}