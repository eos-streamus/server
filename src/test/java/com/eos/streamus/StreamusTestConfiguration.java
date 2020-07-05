package com.eos.streamus;

import com.eos.streamus.controllers.AlbumController;
import com.eos.streamus.controllers.ArtistController;
import com.eos.streamus.controllers.FilmController;
import com.eos.streamus.controllers.SongController;
import com.eos.streamus.controllers.SongPlaylistController;
import com.eos.streamus.dto.validators.AlbumDTOValidator;
import com.eos.streamus.dto.validators.BandMemberDTOValidator;
import com.eos.streamus.dto.validators.MusicianDTOValidator;
import com.eos.streamus.dto.validators.PersonDTOValidator;
import com.eos.streamus.dto.validators.SongPlaylistDTOValidator;
import com.eos.streamus.utils.IDatabaseConnector;
import com.eos.streamus.utils.IResourcePathResolver;
import com.eos.streamus.utils.TestDatabaseConnector;
import com.eos.streamus.utils.TestResourcePathResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StreamusTestConfiguration {
  @Bean
  public IDatabaseConnector databaseConnector() {
    return new TestDatabaseConnector();
  }

  @Bean
  public IResourcePathResolver resourcePathResolver() {
    return new TestResourcePathResolver();
  }

  @Bean
  public SongController songController() {
    return new SongController();
  }

  @Bean
  public FilmController filmController() {
    return new FilmController();
  }

  @Bean
  public ArtistController artistController() {
    return new ArtistController();
  }

  @Bean
  public AlbumController albumController() {
    return new AlbumController();
  }

  @Bean
  public AlbumDTOValidator albumValidator() {
    return new AlbumDTOValidator();
  }

  @Bean
  public MusicianDTOValidator musicianValidator() { return new MusicianDTOValidator(); }

  @Bean
  public PersonDTOValidator personValidator() { return new PersonDTOValidator(); }

  @Bean
  public BandMemberDTOValidator bandMemberValidator() { return new BandMemberDTOValidator(); }

  @Bean
  public SongPlaylistController songPlaylistController() {
    return new SongPlaylistController();
  }

  @Bean
  public SongPlaylistDTOValidator songPlaylistValidator() {
    return new SongPlaylistDTOValidator();
  }

}
