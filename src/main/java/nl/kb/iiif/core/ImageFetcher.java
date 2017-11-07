package nl.kb.iiif.core;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.IOException;

public class ImageFetcher {
    private final HttpClient httpClient;
    private final FileCacher fileCacher;
    private final String resolverFormat;

    public ImageFetcher(HttpClient httpClient, FileCacher fileCacher, String resolverFormat) {
        this.httpClient = httpClient;
        this.fileCacher = fileCacher;
        this.resolverFormat = resolverFormat;
    }


    public File fetch(String identifier) throws IOException {
        if (identifier.startsWith("file://")) {
            final File file = new File(identifier.replace("file://", ""));
            if (file.exists()) {
                return file;
            } else {
                throw new IOException("File " + identifier + " does not exist");
            }
        }
        final File file = fileCacher.fetchLocal(identifier);
        if (file.exists()) {
            return file;
        }

        final HttpResponse resp = httpClient.execute(new HttpGet(String.format(resolverFormat, identifier)));
        fileCacher.save(resp.getEntity().getContent(), file);

        return file;
    }

    public void clear(String identifier) {
        fileCacher.clear(identifier, true);
    }
}
