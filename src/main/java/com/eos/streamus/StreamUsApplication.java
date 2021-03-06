package com.eos.streamus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

//CS BEGIN QUERY
@SpringBootApplication
public class StreamUsApplication {
  public static void main(final String[] args) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.register(StreamusApplicationConfiguration.class);
    ctx.refresh();
    SpringApplication.run(StreamUsApplication.class, args);
  }

}
//CS END QUERY
