package tech.demo.springai;

import org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import tech.demo.springai.components.BetaGeminiAI;

@SpringBootApplication(
    exclude = VertexAiGeminiAutoConfiguration.class
)
public class App {

    @Value("${spring.ai.gemini.base-url}")
    private String geminiBaseUrl;

    @Value("${spring.ai.gemini.api-key}")
    private String geminiApiKey;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    ChatClient chatClient(BetaGeminiAI betaGeminiAI) {
        return new VertexAiGeminiChatClient(betaGeminiAI);
    }

    @Bean
    BetaGeminiAI betaGeminiAI() {
        return BetaGeminiAI.create(geminiBaseUrl, geminiApiKey);
    }

}