package tech.demo.springai.apis;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeAssistantController {

    private ChatClient chatClient;

    public CodeAssistantController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/code/review")
    public String codeReview(String codeSnippet) {
        // TODO: use template for prompting
        Prompt codeReviewPrompt = new Prompt("Tell me a random software engineer joke");
        return chatClient.call(codeReviewPrompt).getResult().getOutput().getContent();
    }
}
