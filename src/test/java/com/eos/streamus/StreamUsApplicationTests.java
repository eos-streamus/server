package com.eos.streamus;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.*;

@SpringBootTest
class StreamUsApplicationTests {
    @Value("${jdbc.url}")
    String driver;
    @Value("${database.user}")
    String user;
    @Value("${database.password}")
    String password;

    @Test
    void contextLoads() {
    }

    @Test
    void connectToDatabase() throws SQLException {
        PGSimpleDataSource ds = new PGSimpleDataSource();  // Empty instance.
        ds.setURL(driver);
        ds.setUser(user);
        ds.setPassword(password);
        ds.getConnection();
    }

}
