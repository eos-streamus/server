package com.eos.streamus.controllers;

import com.eos.streamus.StreamusTestConfiguration;
import com.eos.streamus.utils.IDatabaseConnection;
import com.eos.streamus.utils.IResourcePathResolver;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebMvcTest
@ContextConfiguration(classes = StreamusTestConfiguration.class)
@AutoConfigureMockMvc
abstract class ControllerTests {
  static class JsonSongFactory extends JsonNodeFactory {
    private static final long serialVersionUID = 6068382117192685166L;

  }

  protected static final Path SAMPLE_AUDIO_PATH = Paths.get(
      String.format(
          "src%stest%sresources%ssample-audio.mp3",
          File.separator,
          File.separator,
          File.separator
      )
  );

  protected static final Path SAMPLE_VIDEO_PATH = Paths.get(
      String.format(
          "src%stest%sresources%ssample-video.mp4",
          File.separator,
          File.separator,
          File.separator
      )
  );

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected IResourcePathResolver resourcePathResolver;

  @Autowired
  protected IDatabaseConnection databaseConnection;

}
