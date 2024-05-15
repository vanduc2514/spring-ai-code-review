package tech.demo.springai.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class QualityAssessmentAgent implements AssessmentAgent {

    @Value("classpath:prompts/system-quality-assessment-message.st")
    private Resource systemPromptResource;

    @Value("classpath:/prompts/user-code-review-message.st")
    private Resource userPromptResource;

    @Autowired
    private ChatClient chatClient;

    @Override
    public ChatResponse assessCodeSnippet(String codeSnippet) {
        Message systemMessage = new SystemPromptTemplate(systemPromptResource)
                .createMessage();
        Message userMessage = new PromptTemplate(userPromptResource)
                .createMessage(Map.of("codeSnippet", codeSnippet));
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.call(prompt);
    }

}
