package uk.ac.soton.adauction.example.FetchData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String JDBC_URL = "jdbc:mysql://185.247.116.78:3306/2month_campaign?user=root&password=Group34Southampton!&rewriteBatchedStatements=true";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "Group34Southampton!";

    protected final Connection conn;

    public DatabaseConnection() {

        try {
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return conn;
    }
}
