package nl.kb.iiif.core;

import java.io.File;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.LocalTime;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

public class FileCacher {

  @JsonProperty("dir")
  private String cacheDir;

  private class CacheStats {
    LocalTime lastAccess;
    Long fileSize;

    CacheStats(Long fileSize) {
      this.lastAccess = LocalTime.now();
      this.fileSize = fileSize;
    }
  }

  private final Map<String, CacheStats> cacheMap = new ConcurrentHashMap<>();

  public String getCacheDir() {
    return cacheDir;
  }

  public File fetchLocal(String identifier) {
    final String filename = new String(Base64.getEncoder().encode(identifier.getBytes()));
    final File file = new File(String.format("%s/%s", cacheDir, filename));
    if (file.exists()) {
      cacheMap.put(filename, new CacheStats(file.length()));
    }
    return file;
  }

  public void save(InputStream is, File file) throws IOException {
    IOUtils.copy(is, new FileOutputStream(file));
  }

  public void clear(String identifier) {
      final String filename = new String(Base64.getEncoder().encode(identifier.getBytes()));
      final File file = new File(String.format("%s/%s", cacheDir, filename));
      if (file.exists()) {
          file.delete();
          cacheMap.remove(filename);
      }
  }
}
