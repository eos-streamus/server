package com.eos.streamus.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Scope(value = "singleton")
public class ResourcePathResolver implements IResourcePathResolver {
  @Value("${resourcePath}")
  private String resourcePath;

  @Override
  public String getVideoDir() {
    return String.format("%s%s%s%s", resourcePath, File.separator, "video", File.separator);
  }

  @Override
  public String getAudioDir() {
    return String.format("%s%s%s%s", resourcePath, File.separator, "audio", File.separator);
  }
}
