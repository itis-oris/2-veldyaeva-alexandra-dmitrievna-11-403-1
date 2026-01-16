package com.technocratos;

import com.technocratos.model.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {


    private DBManager connection;

    public PlayerDAO() {
        connection = DBManager.getInstance();
    }
    public int addPlayer(Player player) {
        String sql = "INSERT INTO players (username, score, type) VALUES (?, ?, ?)";
        try (
                Connection connection1 = connection.getConnection();
                PreparedStatement preparedStatement = connection1.prepareStatement(sql)) {

            preparedStatement.setString(1, player.getUsername());
            System.out.println(player);
            preparedStatement.setInt(2, player.getScore());
            preparedStatement.setString(3, player.getType());

            int result = preparedStatement.executeUpdate();

            return result;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public List<Player> getAllPlayers() {
        String sql = "SELECT * FROM players  ORDER BY score DESC LIMIT 7";
        List<Player> players = new ArrayList<>();
        try (
                Connection connection1 = connection.getConnection();
                PreparedStatement preparedStatement = connection1.prepareStatement(sql)
                ) {

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                players.add(
                        new Player(
                                resultSet.getString("username"),
                                resultSet.getInt("score"),
                                resultSet.getString("type")
                        )
                );
            }
            return players;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
