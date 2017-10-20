package nl.kb.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

class Config extends Configuration {

    @NotNull
    private String cacheDir;

    @Valid
    @NotNull
    @JsonProperty("httpClient")
    private HttpClientConfiguration httpClientConfiguration;

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClientConfiguration;
    }

    public String getCacheDir() {
        return cacheDir;
    }
}
