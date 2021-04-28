package com.eos.streamus.writers;

import com.eos.streamus.models.Film;
import com.eos.streamus.models.Resource;
import com.eos.streamus.models.Series;
import com.eos.streamus.models.Song;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JsonResourceWriterFactory {
  private JsonResourceWriterFactory() {}

  public static JsonResourceWriter getWriterFor(final Resource resource) {
    if (resource instanceof Song) {
      return new JsonSongWriter(resource);
    } else if (resource instanceof Film) {
      return new JsonFilmWriter((Film) resource);
    } else if (resource instanceof Series.Episode) {
      return new JsonEpisodeWriter((Series.Episode) resource);
    }
    return new JsonResourceWriter(resource) {
      @Override
      protected ObjectNode addSpecificJsonAttributes(final ObjectNode objectNode) {
        return objectNode;
      }
    };
  }
}
