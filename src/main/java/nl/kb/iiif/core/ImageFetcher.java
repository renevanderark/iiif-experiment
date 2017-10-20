package nl.kb.iiif.core;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageFetcher {
    private final HttpClient httpClient;
    private final String cacheDir;

    public ImageFetcher(HttpClient httpClient, String cacheDir) {
        this.httpClient = httpClient;
        this.cacheDir = cacheDir;
        //TODO: add cache expirer
    }


    public File fetch(String identifier) throws IOException {
        final String filename = new String(Base64.getEncoder().encode(identifier.getBytes()));
        final File file = new File(String.format("%s/%s", cacheDir, filename));
        if (file.exists()) {
            // TODO BUMP TIMEOUT
            return file;
        }

        final HttpResponse resp = httpClient.execute(new HttpGet(String.format("http://resolver.kb.nl/resolve?urn=%s", identifier)));
        IOUtils.copy(resp.getEntity().getContent(), new FileOutputStream(file));

        return file;
    }

    public void clear(String identifier) {
        final String filename = new String(Base64.getEncoder().encode(identifier.getBytes()));
        final File file = new File(String.format("%s/%s", cacheDir, filename));
        if (file.exists()) {
            file.delete();
        }
    }
}
