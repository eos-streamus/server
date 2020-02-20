package com.eos.streamus.models;

import com.eos.streamus.exceptions.NoResultException;

import java.sql.*;

public class Song extends Resource implements SavableEntity {
    private Song(Integer id, String path, String name, Timestamp createdAt, int duration) {
        super(id, path, name, createdAt, duration);
    }

    public Song(String path, String name, int duration) {
        super(path, name, duration);
    }

    public static Song findById(int id, Connection connection) throws SQLException, NoResultException {
        try (PreparedStatement statement = connection.prepareStatement("select * from vsong where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new NoResultException();
                }
                return new Song(
                        rs.getInt("id"),
                        rs.getString("path"),
                        rs.getString("name"),
                        rs.getTimestamp("createdAt"),
                        rs.getInt("duration")
                );
            }
        }
    }

    @Override
    public String tableName() {
        return "Song";
    }

    @Override
    public String primaryKeyName() {
        return "idResource";
    }

    @Override
    public void save(Connection connection) throws SQLException {
        if (this.getId() == null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("select * from createSong(?::varchar(1041), ?::varchar(200), ?)")) {
                preparedStatement.setString(1, getPath());
                preparedStatement.setString(2, getName());
                preparedStatement.setInt(3, getDuration());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("Could not execute statement");
                    }
                    this.setId(resultSet.getInt("id"));
                    this.setCreatedAt(resultSet.getTimestamp("createdAt"));
                }
            }
        } else {
            super.save(connection);
        }
    }
}
