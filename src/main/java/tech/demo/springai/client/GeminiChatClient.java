package tech.demo.springai.client;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

// TODO: Switch to spring implementation if this issue is implemented
// https://github.com/spring-projects/spring-ai/issues/626
public class GeminiChatClient implements ChatClient {

    @Override
    public ChatResponse call(Prompt prompt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'call'");
    }

}
