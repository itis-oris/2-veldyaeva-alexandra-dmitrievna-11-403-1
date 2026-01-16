package com.technocratos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/GAMEDB";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";
    private static Connection connection;

    private static DBManager instance;

    private DBManager() {
        initialize();
    }

    public static DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
            instance.initialize();
        }
        return instance;
    }

    private void initialize() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("PostgreSQL подключен: " + DB_URL);
        } catch (ClassNotFoundException | SQLException e) {

            throw new RuntimeException("Не удалось подключиться к БД", e);
        }
    }


    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Соединение с PostgreSQL закрыто");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка закрытия соединения: " + e.getMessage());
        }
    }
}