package tech.demo.springai.components;

import com.google.cloud.vertexai.api.PredictionServiceClient;

public class BetaPredictionServiceClient extends PredictionServiceClient {

    public BetaPredictionServiceClient(BetaPredictionService betaPredictionService) {
        super(betaPredictionService);
    }

}
