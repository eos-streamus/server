package com.eos.streamus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Nonnull;

//CS BEGIN QUERY
@Configuration
public class StreamusApplicationConfiguration {
  /** @return {@link PasswordEncoder} to use. */
  @Bean
  public PasswordEncoder getPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(@Nonnull CorsRegistry registry) {
        registry.addMapping("/*").allowedOrigins("http://localhost:4200");
      }
    };
  }

}
//CS END QUERY
