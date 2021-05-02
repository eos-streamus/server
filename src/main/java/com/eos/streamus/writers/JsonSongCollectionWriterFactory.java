package com.eos.streamus.writers;

import com.eos.streamus.models.Album;
import com.eos.streamus.models.SongCollection;
import com.eos.streamus.models.SongPlaylist;

final class JsonSongCollectionWriterFactory {
  private JsonSongCollectionWriterFactory() {
  }

  static JsonSongCollectionWriter getWriterFor(final SongCollection songCollection) {
    if (songCollection instanceof Album) {
      return new JsonAlbumWriter((Album) songCollection);
    } else if (songCollection instanceof SongPlaylist) {
      return new JsonSongPlaylistWriter((SongPlaylist) songCollection);
    }
    throw new IllegalArgumentException("Invalid song collection");
  }
}
