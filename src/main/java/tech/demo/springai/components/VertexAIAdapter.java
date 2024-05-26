package tech.demo.springai.components;

import java.io.IOException;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.httpjson.InstantiatingHttpJsonChannelProvider;
import com.google.cloud.vertexai.Transport;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.PredictionServiceClient;
import com.google.cloud.vertexai.api.PredictionServiceSettings;

// Should switch to Spring implementation when this issue is implemented
// https://github.com/spring-projects/spring-ai/issues/626
public class VertexAIAdapter extends VertexAI {

    private String geminiApiKey;

    private PredictionServiceClient predictionServiceClient;

    public VertexAIAdapter(String geminiEndpoint, String geminiApiKey) {
        // Avoid NPE
        super("", "", Transport.REST, new NoOpGoogleCredentials());
        this.geminiApiKey = geminiApiKey;
        setApiEndpoint(geminiEndpoint);
    }

    @Override
    public PredictionServiceClient getPredictionServiceClient() throws IOException {
        if (predictionServiceClient == null) {
            PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                .setEndpoint(getApiEndpoint())
                .setCredentialsProvider(new NoCredentialsProvider())
                .setTransportChannelProvider(InstantiatingHttpJsonChannelProvider.newBuilder().build())
                .build();
            var predictionServiceAdapter = new PredictionServiceAdapter(settings, geminiApiKey);
            predictionServiceClient = new PredictionServiceClient(predictionServiceAdapter) {};
        }
        return predictionServiceClient;
    }

}