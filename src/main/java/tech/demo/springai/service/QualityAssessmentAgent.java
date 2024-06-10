package tech.demo.springai.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.model.function.FunctionCallbackWrapper.Builder.SchemaType;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

@Service
public class QualityAssessmentAgent implements AssessmentAgent {

    @Value("classpath:prompts/system-quality-assessment-message.st")
    private Resource systemPromptResource;

    @Value("classpath:/prompts/user-code-review-message.st")
    private Resource userPromptResource;

    @Autowired
    private StyleGuideService styleGuideService;

    @Autowired
    private StreamingChatModel chatClient;

    @Override
    public Flux<ChatResponse> assessCodeSnippet(String codeSnippet) {
        var chatOptions = VertexAiGeminiChatOptions.builder()
            .withFunctionCallbacks(List.of(FunctionCallbackWrapper.builder(styleGuideService::getStyleGuideFor)
                .withName("GetStyleGuide")
                .withDescription("Get Style Guide for a given language")
                .withSchemaType(SchemaType.OPEN_API_SCHEMA)
                .withInputType(String.class)
                .build()))
            .withModel("gemini-1.5-pro")
            .build();
        var systemMessage = new SystemPromptTemplate(systemPromptResource).createMessage();
        var userMessage = new PromptTemplate(userPromptResource).createMessage(Map.of("codeSnippet", codeSnippet));
        var prompt = new Prompt(List.of(systemMessage, userMessage), chatOptions);
        return chatClient.stream(prompt);
    }

}
