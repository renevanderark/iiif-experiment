package nl.kb.iiif;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.HttpClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import nl.kb.iiif.core.FileCacher;

class Config extends Configuration {

    @NotNull
    @JsonProperty("cache")
    private FileCacher fileCacher;

    @Valid
    @NotNull
    @JsonProperty("httpClient")
    private HttpClientConfiguration httpClientConfiguration;

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClientConfiguration;
    }

    public FileCacher getFileCacher() {
        return fileCacher;
    }
}
