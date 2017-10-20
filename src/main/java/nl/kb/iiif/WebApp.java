package nl.kb.iiif;

import io.dropwizard.Application;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.setup.Environment;
import nl.kb.iiif.core.ImageFetcher;
import nl.kb.iiif.resources.ImageResource;
import nl.kb.utils.NativeUtils;
import org.apache.http.client.HttpClient;

import java.io.IOException;

public class WebApp  extends Application<Config> {
    static {
        try {
            NativeUtils.loadLibraryFromJar("/native/libjp2j.so");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) throws Exception {
        new WebApp().run(args);
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {

        // Standaard apache HttpClient uit dropwizard-client
        final HttpClient httpClient = new HttpClientBuilder(environment)
                .using(config.getHttpClientConfiguration())
                .build(getName());


        environment.jersey().register(new ImageResource(new ImageFetcher(httpClient, config.getCacheDir())));
        // TODO add cache expirer job
    }


}
