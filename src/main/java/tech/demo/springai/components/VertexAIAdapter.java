package tech.demo.springai.components;

import java.io.IOException;

import com.google.cloud.vertexai.Transport;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.PredictionServiceClient;
import com.google.cloud.vertexai.api.PredictionServiceSettings;

// Should switch to Spring implementation when this issue is implemented
// https://github.com/spring-projects/spring-ai/issues/626
public class VertexAIAdapter extends VertexAI {

    private String geminiApiKey;

    private VertexAI delegate;

    private PredictionServiceClient predictionServiceClient;

    public static VertexAIAdapter create(String geminiEndpoint, String geminiApiKey) {
        var dummyVertexAI = new VertexAI.Builder()
                .setProjectId("dummy-project-id")
                .setLocation("dummy-location")
                .setApiEndpoint(geminiEndpoint)
                .setTransport(Transport.REST)
                .setCredentials(new NoOpGoogleCredentials())
                .build();
        return new VertexAIAdapter(dummyVertexAI, geminiApiKey);
    }

    private VertexAIAdapter(VertexAI delegate, String geminiApiKey) {
        super(delegate.getProjectId(), delegate.getLocation());
        this.delegate = delegate;
        this.geminiApiKey = geminiApiKey;
    }

    @Override
    public PredictionServiceClient getPredictionServiceClient() throws IOException {
        if (predictionServiceClient == null) {
            var settings = delegate.getPredictionServiceClient().getSettings();
            var predictionServiceAdapter = new PredictionServiceAdapter((PredictionServiceSettings) settings, geminiApiKey);
            predictionServiceClient = new PredictionServiceClient(predictionServiceAdapter) {};
        }
        return predictionServiceClient;
    }

}