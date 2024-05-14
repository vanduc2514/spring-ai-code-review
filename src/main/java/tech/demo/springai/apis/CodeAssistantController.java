package tech.demo.springai.apis;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
public class CodeAssistantController {

    private ChatClient chatClient;

    private StreamingChatClient streamingChatClient;

    public CodeAssistantController(ChatClient chatClient, StreamingChatClient streamingChatClient) {
        this.chatClient = chatClient;
        this.streamingChatClient = streamingChatClient;
    }

    @GetMapping("/code/review")
    public ChatResponse codeReview(String codeSnippet) {
        // TODO: use template for prompting
        Prompt codeReviewPrompt = new Prompt("Tell me a random software engineer joke");
        return chatClient.call(codeReviewPrompt);
    }

    @GetMapping("/code/review/stream")
    public Flux<ChatResponse> codeReviewStreaming(String codeSnippet) {
        // TODO: use template for prompting
        Prompt codeReviewPrompt = new Prompt("Tell me a random software engineer joke");
        return streamingChatClient.stream(codeReviewPrompt);
    }
}
