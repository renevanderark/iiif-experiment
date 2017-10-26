package nl.kb.iiif.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileCacher {

  @JsonProperty("dir")
  private String cacheDir;

  public String getCacheDir() {
    return cacheDir;
  }
}
