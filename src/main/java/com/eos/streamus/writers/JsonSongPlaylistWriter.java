package com.eos.streamus.writers;

import com.eos.streamus.models.SongPlaylist;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSongPlaylistWriter extends JsonSongCollectionWriter {
  /**
   * {@link SongPlaylist} to write.
   */
  private final SongPlaylist songPlaylist;

  public JsonSongPlaylistWriter(final SongPlaylist songPlaylist) {
    super(songPlaylist);
    this.songPlaylist = songPlaylist;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonNode getSpecificCollectionJson(final ObjectNode objectNode) {
    super.getSpecificCollectionJson(objectNode);
    return objectNode.set("user", new JsonUserWriter(songPlaylist.getUser()).getJson());
  }

}
