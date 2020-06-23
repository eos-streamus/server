package com.eos.streamus;

import com.eos.streamus.controllers.SongController;
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
}
