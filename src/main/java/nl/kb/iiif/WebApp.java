package nl.kb.iiif;

import io.dropwizard.Application;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.kb.iiif.core.CacheCleanerTask;
import nl.kb.iiif.core.FileCacher;
import nl.kb.iiif.core.ImageFetcher;
import nl.kb.iiif.core.ShardFetcher;
import nl.kb.iiif.resources.IIIFServiceResource;
import nl.kb.iiif.resources.IIIFShardedResource;
import nl.kb.iiif.resources.ImagingServiceResource;
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
    public void initialize(Bootstrap<Config> bootstrap) {
        // Support ENV variables in configuration yaml files.
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false))
        );
    }


    @Override
    public void run(Config config, Environment environment) throws Exception {

        // Standaard apache HttpClient uit dropwizard-client
        final HttpClient httpClient = new HttpClientBuilder(environment)
                .using(config.getHttpClientConfiguration())
                .build(getName());


        final FileCacher fileCacher = config.getFileCacher();
        final ImageFetcher imageFetcher = new ImageFetcher(httpClient, fileCacher, config.getResolverFormat());
        environment.jersey().register(new IIIFServiceResource(imageFetcher));
        environment.jersey().register(new IIIFShardedResource(new ShardFetcher()));
        environment.jersey().register(new ImagingServiceResource(imageFetcher));

        environment.lifecycle().manage(new ManagedPeriodicTask(new CacheCleanerTask(fileCacher)));
    }


}
