package ru.mirea.project.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    // public — чтобы экспорт в Excel подключался к той же базе, а не хранил свою копию настроек
    public static final String URL = "jdbc:postgresql://localhost:5437/pet_hotel";
    public static final String USER = "postgres";
    public static final String PASSWORD = "postgres";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
