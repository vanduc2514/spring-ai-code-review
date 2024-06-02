package tech.demo.springai.components;

import java.io.IOException;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.httpjson.InstantiatingHttpJsonChannelProvider;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.PredictionServiceClient;
import com.google.cloud.vertexai.api.PredictionServiceSettings;

// Should switch to Spring implementation when this issue is implemented
// https://github.com/spring-projects/spring-ai/issues/626
public class VertexAIAdapter extends VertexAI {

    private String geminiApiKey;

    private String geminiEndpoint;

    private PredictionServiceClient predictionServiceClient;

    public VertexAIAdapter(String geminiEndpoint, String geminiApiKey) {
        // Avoid exception
        super("dummy", "dummy");
        this.geminiApiKey = geminiApiKey;
        this.geminiEndpoint = geminiEndpoint;
    }

    @Override
    public PredictionServiceClient getPredictionServiceClient() {
        if (predictionServiceClient == null) {
            try {
                PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                    .setEndpoint(geminiEndpoint)
                    .setCredentialsProvider(new NoCredentialsProvider())
                    .setTransportChannelProvider(InstantiatingHttpJsonChannelProvider.newBuilder().build())
                    .build();
                var predictionServiceAdapter = new PredictionServiceAdapter(settings, geminiApiKey);
                predictionServiceClient = new PredictionServiceClient(predictionServiceAdapter) {};
            } catch (IOException exception) {
                throw new RuntimeException("Cannot create PredictionServiceClient");
            }
        }
        return predictionServiceClient;
    }

}