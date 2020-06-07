package com.eos.streamus.writers;

import com.eos.streamus.models.SongCollection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSongCollectionWriter extends JsonCollectionWriter {
  private final SongCollection songCollection;

  protected JsonSongCollectionWriter(final SongCollection songCollection) {
    super(songCollection);
    this.songCollection = songCollection;
  }

  @Override
  protected JsonNode getSpecificCollectionJson(final ObjectNode objectNode) {
    ArrayNode tracks = objectNode.putArray("tracks");
    for (SongCollection.Track track : songCollection.getTracks()) {
      tracks.add(new JsonTrackWriter(track).getJson());
    }
    return objectNode;
  }

}
