package nl.kb.iiif.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ShardFetcher {

    public InputStream fetchMetadata(String identifier) throws IOException {
        if (identifier.startsWith("file://")) {
            final File file = new File(String.format("%s/jp2head.json",
                    identifier.replace("file://", "")
            ));
            return new FileInputStream(file);
        }
        throw new UnsupportedOperationException("TODO");
    }
}
