package com.technocratos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {

    private static Connection connection;
    private static final PropertiesLoader propsLoader =  new PropertiesLoader();


    private static DBManager instance;

    private DBManager() {}

    public static DBManager getInstance() {
        if (instance == null) {
            instance = new DBManager();
            instance.initialize();
        }
        return instance;
    }

    private void initialize() {
        try {
            Class.forName(propsLoader.getProperty("database.driver"));

        } catch (ClassNotFoundException e) {

            throw new RuntimeException("Не удалось подключиться к БД", e);
        }
    }


    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    propsLoader.getProperty("database.url"),
                    propsLoader.getProperty("database.username"),
                    propsLoader.getProperty("database.password")
            );
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