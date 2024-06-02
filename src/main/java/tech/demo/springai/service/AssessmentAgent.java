package tech.demo.springai.service;


import org.springframework.ai.chat.model.ChatResponse;

import reactor.core.publisher.Flux;

public interface AssessmentAgent {

    Flux<ChatResponse> assessCodeSnippet(String codeSnippet);

}
