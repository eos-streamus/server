package com.eos.streamus.utils;

public interface IResourcePathResolver {

  /** @return Video storage directory path. */
  String getVideoDir();

  /** @return Audio storage directory path. */
  String getAudioDir();
}
