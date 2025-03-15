package uk.ac.soton.adauction.example.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String LOGS_URL = "jdbc:mysql://185.247.116.78:3306/2month_campaign?user=root&password=Group34Southampton!&rewriteBatchedStatements=true";
    private static final String USERDATA_URL = "jdbc:mysql://185.247.116.78:3306/userdata";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "Group34Southampton!";

    protected static Connection conn;

    static {
        try {
            conn = DriverManager.getConnection(LOGS_URL, JDBC_USER, JDBC_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static Connection getLogsConnection() {
        try {
            conn = DriverManager.getConnection(LOGS_URL, JDBC_USER, JDBC_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
    public static Connection getUserDataConnection() {
        try {
            conn = DriverManager.getConnection(USERDATA_URL, JDBC_USER, JDBC_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }
}
