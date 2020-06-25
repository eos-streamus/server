package com.eos.streamus;

import com.eos.streamus.controllers.ArtistController;
import com.eos.streamus.controllers.FilmController;
import com.eos.streamus.controllers.SongController;
import com.eos.streamus.payloadmodels.validators.BandMemberValidator;
import com.eos.streamus.payloadmodels.validators.MusicianValidator;
import com.eos.streamus.payloadmodels.validators.PersonValidator;
import com.eos.streamus.utils.IDatabaseConnection;
import com.eos.streamus.utils.IResourcePathResolver;
import com.eos.streamus.utils.TestDatabaseConnection;
import com.eos.streamus.utils.TestResourcePathResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StreamusTestConfiguration {
  @Bean
  public IDatabaseConnection databaseConnection() {
    return new TestDatabaseConnection();
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
  public MusicianValidator musicianValidator() { return new MusicianValidator(); }

  @Bean
  public PersonValidator personValidator() { return new PersonValidator(); }

  @Bean
  public BandMemberValidator bandMemberValidator() { return new BandMemberValidator(); }
}
