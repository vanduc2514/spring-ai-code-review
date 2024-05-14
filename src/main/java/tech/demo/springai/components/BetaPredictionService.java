package tech.demo.springai.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.api.gax.httpjson.ApiMethodDescriptor;
import com.google.api.gax.httpjson.HttpJsonCallSettings;
import com.google.api.gax.httpjson.HttpJsonCallableFactory;
import com.google.api.gax.httpjson.ProtoMessageRequestFormatter;
import com.google.api.gax.httpjson.ProtoMessageResponseParser;
import com.google.api.gax.httpjson.ProtoRestSerializer;
import com.google.api.gax.rpc.ClientContext;
import com.google.api.gax.rpc.UnaryCallSettings;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.cloud.vertexai.api.GenerateContentRequest;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.stub.PredictionServiceStub;
import com.google.protobuf.TypeRegistry;

public class BetaPredictionService extends PredictionServiceStub {

    private static final String DEFAULT_MODEL = "gemini-pro";

    private PredictionServiceStub delegate;

    private UnaryCallSettings<GenerateContentRequest, GenerateContentResponse> generateContentSettings;

    private ClientContext clientContext;

    private String geminiApiKey;

    private TypeRegistry typeRegistry;

    public BetaPredictionService(
            PredictionServiceStub delegate,
            UnaryCallSettings<GenerateContentRequest, GenerateContentResponse> generateContentSettings,
            ClientContext clientContext,
            String geminiApiKey) {
        this.delegate = delegate;
        this.generateContentSettings = generateContentSettings;
        this.clientContext = clientContext;
        this.geminiApiKey = geminiApiKey;
        typeRegistry = TypeRegistry.newBuilder().build();
    }

    @Override
    public UnaryCallable<GenerateContentRequest, GenerateContentResponse> generateContentCallable() {
        HttpJsonCallSettings<GenerateContentRequest, GenerateContentResponse> httpJsonCallSettings = HttpJsonCallSettings
                .<GenerateContentRequest, GenerateContentResponse>newBuilder()
                .setMethodDescriptor(getApiMethodDescriptor())
                .setTypeRegistry(typeRegistry)
                .build();
        return HttpJsonCallableFactory.createUnaryCallable(
                httpJsonCallSettings,
                generateContentSettings,
                clientContext);
    }

    private ApiMethodDescriptor<GenerateContentRequest, GenerateContentResponse> getApiMethodDescriptor() {
        ProtoMessageRequestFormatter<GenerateContentRequest> requestFormatter =
            ProtoMessageRequestFormatter.<GenerateContentRequest>newBuilder()
                .setPath(
                    "/v1beta/models/{model=*}:generateContent",
                    request -> {
                        Map<String, String> fields = new HashMap<>();
                        fields.put("model", DEFAULT_MODEL);
                        return fields;
                })
                .setQueryParamsExtractor(request -> {
                    Map<String, List<String>> fields = new HashMap<>();
                    fields.put("key", Collections.singletonList(geminiApiKey));
                    return fields;
                })
                .setRequestBodyExtractor(request -> ProtoRestSerializer.create()
                    .toBody("*", request.toBuilder().clearModel().build(),false))
                .build();
        ProtoMessageResponseParser<GenerateContentResponse> responseParser =
            ProtoMessageResponseParser.<GenerateContentResponse>newBuilder()
                .setDefaultInstance(GenerateContentResponse.getDefaultInstance())
                .setDefaultTypeRegistry(typeRegistry)
                .build();
        return ApiMethodDescriptor.<GenerateContentRequest, GenerateContentResponse>newBuilder()
                .setFullMethodName("google.cloud.aiplatform.v1.PredictionService/GenerateContent")
                .setHttpMethod("POST")
                .setType(ApiMethodDescriptor.MethodType.UNARY)
                .setRequestFormatter(requestFormatter)
                .setResponseParser(responseParser)
                .build();
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public void shutdownNow() {
        delegate.shutdownNow();
    }

    @Override
    public boolean awaitTermination(long duration, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(duration, unit);
    }

    @Override
    public void close() {
        delegate.close();
    }

}
