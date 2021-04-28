package com.eos.streamus.writers;

import com.eos.streamus.models.Album;
import com.eos.streamus.models.Artist;
import com.eos.streamus.models.Band;
import com.eos.streamus.models.Musician;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonAlbumWriter extends JsonSongCollectionWriter {
  /** {@link Album} to write. */
  private final Album album;

  public JsonAlbumWriter(final Album album) {
    super(album);
    this.album = album;
  }

  /** {@inheritDoc} */
  @Override
  protected JsonNode getSpecificCollectionJson(final ObjectNode objectNode) {
    super.getSpecificCollectionJson(objectNode);
    ArrayNode artists = objectNode.putArray("artists");
    for (Artist artist : album.getArtists()) {
      if (artist instanceof Band) {
        artists.add(new JsonBandWriter((Band) artist).getJson());
      } else {
        artists.add(new JsonMusicianWriter((Musician) artist).getJson());
      }
    }
    return objectNode;
  }

}
