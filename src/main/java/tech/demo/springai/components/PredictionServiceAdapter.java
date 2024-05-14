package tech.demo.springai.components;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.api.gax.core.BackgroundResource;
import com.google.api.gax.core.BackgroundResourceAggregation;
import com.google.api.gax.httpjson.ApiMethodDescriptor;
import com.google.api.gax.httpjson.HttpJsonCallSettings;
import com.google.api.gax.httpjson.HttpJsonCallableFactory;
import com.google.api.gax.httpjson.ProtoMessageRequestFormatter;
import com.google.api.gax.httpjson.ProtoMessageResponseParser;
import com.google.api.gax.httpjson.ProtoRestSerializer;
import com.google.api.gax.rpc.ClientContext;
import com.google.api.gax.rpc.ServerStreamingCallable;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.cloud.vertexai.api.GenerateContentRequest;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.PredictionServiceSettings;
import com.google.cloud.vertexai.api.stub.PredictionServiceStub;
import com.google.protobuf.TypeRegistry;

public class PredictionServiceAdapter extends PredictionServiceStub {

    private static final String DEFAULT_MODEL = "gemini-pro";

    private PredictionServiceSettings predictionServiceSettings;

    private ClientContext clientContext;

    private BackgroundResource backgroundResources;

    private String geminiApiKey;

    private TypeRegistry typeRegistry;

    public PredictionServiceAdapter(
            PredictionServiceSettings predictionServiceSettings,
            String geminiApiKey) throws IOException {
        this.predictionServiceSettings = predictionServiceSettings;
        this.geminiApiKey = geminiApiKey;
        typeRegistry = TypeRegistry.newBuilder().build();
        clientContext = ClientContext.create(predictionServiceSettings);
        backgroundResources = new BackgroundResourceAggregation(clientContext.getBackgroundResources());
    }

    @Override
    public UnaryCallable<GenerateContentRequest, GenerateContentResponse> generateContentCallable() {
        HttpJsonCallSettings<GenerateContentRequest, GenerateContentResponse> httpJsonCallSettings = HttpJsonCallSettings
                .<GenerateContentRequest, GenerateContentResponse>newBuilder()
                .setMethodDescriptor(createApiMethodDescriptor("generateContent", ApiMethodDescriptor.MethodType.UNARY))
                .setTypeRegistry(typeRegistry)
                .build();
        return HttpJsonCallableFactory.createUnaryCallable(
                httpJsonCallSettings,
                predictionServiceSettings.generateContentSettings(),
                clientContext);
    }

    @Override
    public ServerStreamingCallable<GenerateContentRequest, GenerateContentResponse> streamGenerateContentCallable() {
        HttpJsonCallSettings<GenerateContentRequest, GenerateContentResponse> httpJsonCallSettings = HttpJsonCallSettings
                .<GenerateContentRequest, GenerateContentResponse>newBuilder()
                .setMethodDescriptor(createApiMethodDescriptor("streamGenerateContent", ApiMethodDescriptor.MethodType.SERVER_STREAMING))
                .setTypeRegistry(typeRegistry)
                .build();
        return HttpJsonCallableFactory.createServerStreamingCallable(
                httpJsonCallSettings,
                predictionServiceSettings.streamGenerateContentSettings(),
                clientContext);
    }

    private ApiMethodDescriptor<GenerateContentRequest, GenerateContentResponse> createApiMethodDescriptor(String methodName, ApiMethodDescriptor.MethodType methodType) {
        ProtoMessageRequestFormatter<GenerateContentRequest> requestFormatter =
            ProtoMessageRequestFormatter.<GenerateContentRequest>newBuilder()
                .setPath("/v1beta/models/{model=*}:" + methodName, request -> Map.of("model", DEFAULT_MODEL))
                .setQueryParamsExtractor(request -> Map.of("key", List.of(geminiApiKey)))
                .setRequestBodyExtractor(request -> ProtoRestSerializer.create().toBody("*", request.toBuilder().clearModel().build(),false))
                .build();
        ProtoMessageResponseParser<GenerateContentResponse> responseParser =
            ProtoMessageResponseParser.<GenerateContentResponse>newBuilder()
                .setDefaultInstance(GenerateContentResponse.getDefaultInstance())
                .setDefaultTypeRegistry(typeRegistry)
                .build();
        return ApiMethodDescriptor.<GenerateContentRequest, GenerateContentResponse>newBuilder()
                .setFullMethodName("google.cloud.aiplatform.v1.PredictionService/" + methodName)
                .setHttpMethod("POST")
                .setType(methodType)
                .setRequestFormatter(requestFormatter)
                .setResponseParser(responseParser)
                .build();
    }

    @Override
    public void shutdown() {
        backgroundResources.shutdown();
    }

    @Override
    public boolean isShutdown() {
        return backgroundResources.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return backgroundResources.isTerminated();
    }

    @Override
    public void shutdownNow() {
        backgroundResources.shutdownNow();
    }

    @Override
    public boolean awaitTermination(long duration, TimeUnit unit) throws InterruptedException {
        return backgroundResources.awaitTermination(duration, unit);
    }

    @Override
    public void close() {
        try {
            backgroundResources.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
