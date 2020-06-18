package com.eos.streamus.writers;

import com.eos.streamus.models.SongPlaylist;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSongPlaylistWriter extends JsonSongCollectionWriter {
  private final SongPlaylist songPlaylist;
  public JsonSongPlaylistWriter(final SongPlaylist songPlaylist) {
    super(songPlaylist);
    this.songPlaylist = songPlaylist;
  }

  @Override
  protected JsonNode getSpecificCollectionJson(final ObjectNode objectNode) {
    super.getSpecificCollectionJson(objectNode);
    return objectNode.set("user", new JsonUserWriter(songPlaylist.getUser()).getJson());
  }

}
