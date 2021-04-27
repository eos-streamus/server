package com.eos.streamus.writers;

import com.eos.streamus.models.Resource;
import com.eos.streamus.models.Series;
import com.eos.streamus.utils.Pair;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSeriesWriter extends JsonCollectionWriter {
  /**
   * {@link Series} to write.
   */
  private final Series series;

  protected JsonSeriesWriter(final Series series) {
    super(series);
    this.series = series;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected JsonNode getSpecificCollectionJson(final ObjectNode objectNode) {
    ArrayNode episodes = objectNode.putArray("episodes");
    for (Pair<Integer, Resource> entry : series.getContent()) {
      episodes.add(new JsonEpisodeWriter((Series.Episode) entry.getValue()).getJson());
    }
    return objectNode;
  }

}
