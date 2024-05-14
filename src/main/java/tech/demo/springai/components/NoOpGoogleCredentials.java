package tech.demo.springai.components;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.auth.Credentials;

public class NoOpGoogleCredentials extends Credentials {

    @Override
    public String getAuthenticationType() {
        return "No-Op";
    }

    @Override
    public Map<String, List<String>> getRequestMetadata(URI uri) throws IOException {
        return new HashMap<>();
    }

    @Override
    public boolean hasRequestMetadata() {
        return false;
    }

    @Override
    public boolean hasRequestMetadataOnly() {
        return false;
    }

    @Override
    public void refresh() throws IOException {
        // No-op
    }

}
