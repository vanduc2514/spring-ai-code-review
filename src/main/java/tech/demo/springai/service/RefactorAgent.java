package tech.demo.springai.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class RefactorAgent {

    @Value("classpath:prompts/system-refactor-code-message.st")
    private Resource systemPromptResource;

    @Value("classpath:/prompts/user-refactor-code-message.st")
    private Resource userPromptResource;

    @Autowired
    private StreamingChatModel chatClient;

    public Flux<ChatResponse> refactorCode(
        String codeSnippet,
        String logicAssessment,
        String qualityAssessment
    ) {
        var systemMessage = new SystemPromptTemplate(systemPromptResource).createMessage();
        var userMessage = new PromptTemplate(userPromptResource).createMessage(
            Map.of(
                "codeSnippet", codeSnippet,
                "logicAssessment", logicAssessment,
                "qualityAssessment", qualityAssessment
            ));
        var prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.stream(prompt);
    }
}
