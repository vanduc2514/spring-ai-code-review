package tech.demo.springai;

import org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import tech.demo.springai.components.VertexAIAdapter;

@SpringBootApplication(exclude = VertexAiGeminiAutoConfiguration.class)
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Configuration
    private static class ChatModelConfiguration {

        @Autowired
        private OllamaApi ollamaApi;

        @Value("${spring.ai.gemini.base-url}")
        private String geminiBaseUrl;

        @Value("${spring.ai.gemini.api-key}")
        private String geminiApiKey;

        private VertexAIAdapter vertexAIAdapter;

        @SuppressWarnings("unused")
        ChatModelConfiguration() {
        }

        @PostConstruct
        void init() {
            vertexAIAdapter = new VertexAIAdapter(geminiBaseUrl, geminiApiKey);
        }

        @Bean
        @Primary
        @RequestScope
        ChatModel chatModel(HttpServletRequest request) {
            String headerValue = request.getHeader("X-Chat-Model");
            if (headerValue != null && headerValue.contains("gemini")) {
                return new VertexAiGeminiChatModel(vertexAIAdapter);
            } else if (headerValue != null && headerValue.contains("ollama-phi3")) {
                return new OllamaChatModel(ollamaApi, new OllamaOptions().withModel("phi3"));
            } else if (headerValue != null && headerValue.contains("ollama-tinyllama")) {
                return new OllamaChatModel(ollamaApi, new OllamaOptions().withModel("tinyllama"));
            }
            return new VertexAiGeminiChatModel(vertexAIAdapter);
        }

    }

}