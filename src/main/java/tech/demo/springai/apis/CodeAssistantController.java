package tech.demo.springai.apis;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class CodeAssistantController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private StreamingChatClient streamingChatClient;

    @GetMapping("/debug")
    public ChatResponse debug(@RequestParam String prompt) {
        return chatClient.call(new Prompt(prompt));
    }

    @GetMapping("/debug/stream")
    public Flux<ChatResponse> debugStream(@RequestParam String prompt) {
        return streamingChatClient.stream(new Prompt(prompt));
    }

    @GetMapping("/code/review")
    public ChatResponse codeReview(String codeSnippet) {
        throw new UnsupportedOperationException("Not Yet Implemneted");
    }

    @GetMapping("/code/review/stream")
    public Flux<ChatResponse> codeReviewStreaming(String codeSnippet) {
        throw new UnsupportedOperationException("Not Yet Implemneted");
    }
}
