package com.eos.streamus;

import com.eos.streamus.controllers.SongController;
import com.eos.streamus.utils.IDatabaseConnection;
import com.eos.streamus.utils.ResourcePathResolver;
import com.eos.streamus.utils.TestDatabaseConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StreamusTestConfiguration {
  @Bean
  public IDatabaseConnection databaseConnection() {
    return new TestDatabaseConnection();
  }

  @Bean
  public ResourcePathResolver resourcePathResolver() {
    return new ResourcePathResolver();
  }

  @Bean
  public SongController songController() {
    return new SongController();
  }
}
