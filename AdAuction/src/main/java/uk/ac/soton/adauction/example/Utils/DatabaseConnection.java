package uk.ac.soton.adauction.example.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String LOGS_URL = "jdbc:mysql://185.247.116.78:3306/2month_campaign?user=root&password=Group34Southampton!&rewriteBatchedStatements=true";
    private static final String USERDATA_URL = "jdbc:mysql://185.247.116.78:3306/userdata";
    private static final String LOCAL_LOGS_URL = "jdbc:sqlite:logs.db";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "Group34Southampton!";

    private static Connection remoteLogsConnection;
    private static Connection usersConnection;
    private static Connection localLogsConnection;


    static {
        try {
            remoteLogsConnection = DriverManager.getConnection(LOGS_URL, JDBC_USER, JDBC_PASSWORD);
            usersConnection = DriverManager.getConnection(USERDATA_URL, JDBC_USER, JDBC_PASSWORD);
            localLogsConnection = DriverManager.getConnection(LOCAL_LOGS_URL);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getRemoteUsersConnection() {
        return usersConnection;
    }
    public static Connection getRemoteLogsConnection() {
        return remoteLogsConnection;
    }
    public static Connection getLocalLogsConnection() {
        return localLogsConnection;
    }

}
