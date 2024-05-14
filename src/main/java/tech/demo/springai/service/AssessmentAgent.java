package tech.demo.springai.service;

import org.springframework.ai.chat.ChatResponse;

public interface AssessmentAgent {

    ChatResponse assessCodeSnippet(String codeSnippet);

}
