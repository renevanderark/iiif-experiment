package nl.kb.iiif.core;

import java.io.File;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Base64;

public class FileCacher {

  @JsonProperty("dir")
  private String cacheDir;

  public String getCacheDir() {
    return cacheDir;
  }

  public File fetchLocal(String identifier) {
    final String filename = new String(Base64.getEncoder().encode(identifier.getBytes()));
    return new File(String.format("%s/%s", cacheDir, filename));
  }

  public void clear(String identifier) {
      final String filename = new String(Base64.getEncoder().encode(identifier.getBytes()));
      final File file = new File(String.format("%s/%s", cacheDir, filename));
      if (file.exists()) {
          file.delete();
      }
  }
}
