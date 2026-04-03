package com.veritas;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface VeritasCrew {

    @SystemMessage({
            "You are a Senior Deepfake Forensic Analyst.",
            "Your job is to look for 'Uncanny Valley' artifacts, pulse detection, and lighting inconsistencies.",
            "Provide a confidence score from 0-100%."
    })
    String analyzeMedia(@UserMessage String description);
}