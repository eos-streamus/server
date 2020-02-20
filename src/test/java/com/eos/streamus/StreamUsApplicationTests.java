package com.eos.streamus;

import com.eos.streamus.exceptions.NoResultException;
import com.eos.streamus.models.Song;
import com.eos.streamus.utils.DatabaseConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StreamUsApplicationTests {
    @Autowired
    protected DatabaseConnection databaseConnection = null;

    @Test
    void connectToDatabase() {
        assertDoesNotThrow(() -> {
            try (Connection ignored = databaseConnection.getConnection()) {
            }
        });
    }

    @Test
    void testSongCRUD() throws SQLException, NoResultException, ClassNotFoundException {
        // Create
        try (Connection connection = databaseConnection.getConnection()) {
            Song song = new Song(String.format("test%d.mp3", new Date().getTime()), "Test song", 100);
            song.save(connection);

            // Read
            Song song2 = Song.findById(song.getId(), connection);
            assertEquals(song.getId(), song2.getId());

            // Update
            song.setName("Changed name");
            song.save(connection);

            song2 = Song.findById(song.getId(), connection);
            assertEquals(song.getName(), song2.getName());

            // Delete
            song.delete(connection);
            assertThrows(NoResultException.class, () -> Song.findById(song.getId(), connection));
        }

    }
}
