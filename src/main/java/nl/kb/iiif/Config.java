package nl.kb.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;
import nl.kb.iiif.core.FileCacher;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

class Config extends Configuration {

    @NotNull
    @JsonProperty("cache")
    private FileCacher fileCacher;

    @Valid
    @NotNull
    @JsonProperty("httpClient")
    private HttpClientConfiguration httpClientConfiguration;

    @NotNull
    @JsonProperty("resolverFormat")
    private String resolverFormat;

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClientConfiguration;
    }

    public FileCacher getFileCacher() {
        return fileCacher;
    }

    public String getResolverFormat() {
        return resolverFormat;
    }
}
