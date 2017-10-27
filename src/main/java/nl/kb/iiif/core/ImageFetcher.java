package nl.kb.iiif.core;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.IOException;

public class ImageFetcher {
    private final HttpClient httpClient;
    private final FileCacher fileCacher;

    public ImageFetcher(HttpClient httpClient, FileCacher fileCacher) {
        this.httpClient = httpClient;
        this.fileCacher = fileCacher;
        //TODO: add cache expirer
    }


    public File fetch(String identifier) throws IOException {
        final File file = fileCacher.fetchLocal(identifier);
        if (file.exists()) {
            // TODO BUMP TIMEOUT
            return file;
        }

        final HttpResponse resp = httpClient.execute(new HttpGet(String.format("http://resolver.kb.nl/resolve?urn=%s", identifier)));
        fileCacher.save(resp.getEntity().getContent(), file);

        return file;
    }

    public void clear(String identifier) {
        fileCacher.clear(identifier, true);
    }
}
