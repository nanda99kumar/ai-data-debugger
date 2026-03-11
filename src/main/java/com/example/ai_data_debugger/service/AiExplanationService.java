package com.example.ai_data_debugger.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
@Service
public class AiExplanationService {

    private final ChatClient chatClient;

    public AiExplanationService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String explainDataIssues(String analysis) {

        String prompt = """
        You are a data engineering expert.

        Analyze this dataset report and explain problems.

        Also suggest SQL fixes.

        Report:
        """ + analysis;

        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }
}