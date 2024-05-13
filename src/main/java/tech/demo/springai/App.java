package tech.demo.springai;

import org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration;
import org.springframework.ai.chat.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import tech.demo.springai.client.GeminiChatClient;

@SpringBootApplication(
    exclude = VertexAiGeminiAutoConfiguration.class
)
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public ChatClient chatClient() {
        return new GeminiChatClient();
    }

}