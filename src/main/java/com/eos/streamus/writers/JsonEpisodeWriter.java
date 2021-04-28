package com.eos.streamus.writers;

import com.eos.streamus.models.Series;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonEpisodeWriter extends JsonResourceWriter {
  /** {@link com.eos.streamus.models.Series .com.eos.streamus.models.Series.Episode} to write. */
  private final Series.Episode episode;

  public JsonEpisodeWriter(final Series.Episode episode) {
    super(episode);
    this.episode = episode;
  }

  /** {@inheritDoc} */
  @Override
  protected ObjectNode addSpecificJsonAttributes(final ObjectNode objectNode) {
    return objectNode
        .put("idSeries", episode.getSeries().getId())
        .put("seasonNumber", episode.getSeasonNumber())
        .put("episodeNumber", episode.getEpisodeNumber());
  }

}
